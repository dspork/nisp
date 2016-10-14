/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nisp.services

import java.util.TimeZone

import org.joda.time.{DateTimeZone, LocalDate, Period, PeriodType}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.enums.Scenario
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.models.{SPAmountModel, StatePension, StatePensionAmount, StatePensionAmounts, StatePensionExclusion}
import uk.gov.hmrc.nisp.services.reference.QualifyingYearsAmountService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait StatePensionService {
  val npsConnector: NpsConnector
  val metrics: Metrics
  val forecastingService: ForecastingService
  val citizenDetailsService: CitizenDetailsService
  def now: LocalDate

  def getStatement(nino: Nino)(implicit request: HeaderCarrier): Future[Either[StatePensionExclusion, StatePension]] = {

      val npsSummaryF = npsConnector.connectToSummary(nino)
      val npsNationalInsuranceRecordF = npsConnector.connectToNIRecord(nino)
      val npsLiabilitiesF = npsConnector.connectToLiabilities(nino)
      val npsSchemeMembershipsF = npsConnector.connectToSchemeMembership(nino)
      val manualCorrespondenceIndicatorF = citizenDetailsService.retrieveMCIStatus(nino)

      for (
        summary <- npsSummaryF;
        nationalInsuranceRecord <- npsNationalInsuranceRecordF;
        liabilities <- npsLiabilitiesF;
        schemeMemberships <- npsSchemeMembershipsF;
        manualCorrespondenceIndicator <- manualCorrespondenceIndicatorF
      ) yield {

        val exclusions = ExclusionsService(
          summary.isAbroad,
          summary.rreToConsider == 1,
          summary.dateOfDeath,
          summary.nino,
          liabilities,
          summary.npsStatePensionAmount.nspEntitlement,
          SPCurrentAmountService.calculate(summary.npsStatePensionAmount.npsAmountA2016,
            summary.npsStatePensionAmount.npsAmountB2016),
          NpsDate(now),
          summary.spaDate,
          summary.sex,
          manualCorrespondenceIndicator
        ).getSPExclusions.exclusions

        if (exclusions.nonEmpty) {

          metrics.exclusion(exclusions)

          Left(StatePensionExclusion(
            exclusions,
            pensionAge = new Period(summary.dateOfBirth.localDate, summary.spaDate.localDate).getYears,
            pensionDate = summary.spaDate.localDate
          ))

        } else {

          val purgedNationalInsuranceRecord = nationalInsuranceRecord.purge(summary.finalRelevantYear)

          val forecast = ForecastingService.getForecastAmount(
            schemeMemberships,
            earningsIncludedUpTo = summary.earningsIncludedUpTo,
            currentQualifyingYears = summary.nspQualifyingYears,
            amountA = summary.npsStatePensionAmount.npsAmountA2016,
            amountB = summary.npsStatePensionAmount.npsAmountB2016,
            lastYearEarnings = purgedNationalInsuranceRecord.niTaxYears.
              find(_.taxYear == summary.earningsIncludedUpTo.taxYear).map(_.primaryPaidEarnings).getOrElse(0),
            finalRelevantYear = summary.finalRelevantYear,
            forecastAmount = summary.pensionForecast.forecastAmount,
            forecastAmount2016 = summary.pensionForecast.forecastAmount2016,
            lastYearQualifying = purgedNationalInsuranceRecord.niTaxYears.
              find(_.taxYear == summary.earningsIncludedUpTo.taxYear).exists(_.qualifying),
            nino = nino,
            fillableGaps = nationalInsuranceRecord.nonQualifyingYearsPayable,
            currentAmount = SPAmountModel(summary.npsStatePensionAmount.nspEntitlement)
          )

          metrics.summary(forecast.forecastAmount.week, summary.npsStatePensionAmount.nspEntitlement, schemeMemberships.nonEmpty,
            forecast.scenario.equals(Scenario.ForecastOnly), new Period(summary.dateOfBirth.localDate, now, PeriodType.yearMonthDay()).getYears, forecast.scenario,
            forecast.personalMaximum.week, forecast.yearsLeftToWork, ForecastingService.getMqpScenario(summary.nspQualifyingYears, summary.yearsUntilPensionAge,
              nationalInsuranceRecord.nonQualifyingYearsPayable))

          Right(
            StatePension(
            summary.earningsIncludedUpTo.localDate,
            amounts = StatePensionAmounts(
              protectedPayment = forecast.oldRulesCustomer,
              current = StatePensionAmount(None, None, summary.npsStatePensionAmount.nspEntitlement),
              forecast = StatePensionAmount(Some(forecast.yearsLeftToWork), None, forecast.forecastAmount.week),
              maximum = StatePensionAmount(Some(forecast.yearsLeftToWork), Some(forecast.minGapsToFillToReachMaximum), forecast.personalMaximum.week),
              cope = StatePensionAmount(None, None, summary.npsStatePensionAmount.npsAmountB2016.rebateDerivedAmount)
            ),
            pensionAge = new Period(summary.dateOfBirth.localDate, summary.spaDate.localDate).getYears,
            pensionDate = summary.spaDate.localDate,
            finalRelevantYear = summary.finalRelevantYear,
            numberOfQualifyingYears = summary.nspQualifyingYears,
            pensionSharingOrder = summary.pensionShareOrderCOEG != 0,
            currentFullWeeklyPensionAmount = QualifyingYearsAmountService.maxAmount
          ))
        }
      }
  }
}

object StatePensionService extends StatePensionService {
  override val npsConnector: NpsConnector = NpsConnector
  override def now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
  override val forecastingService: ForecastingService = ForecastingService
  override val metrics: Metrics = Metrics
  override val citizenDetailsService: CitizenDetailsService = CitizenDetailsService
}
