/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.nisp.models._
import uk.gov.hmrc.nisp.models.enums.Scenario
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.utils.{NISPConstants, WithCurrentDate}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

object SPResponseService extends SPResponseService {
  override val nps: NpsConnector = NpsConnector
  override def now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
  override val forecastingService: ForecastingService = ForecastingService
  override val metrics: Metrics = Metrics
  override val citizenDetailsService: CitizenDetailsService = CitizenDetailsService
}

trait SPResponseService extends WithCurrentDate {
  val forecastingService: ForecastingService
  val nps: NpsConnector
  val metrics: Metrics
  val citizenDetailsService: CitizenDetailsService

  def getSPResponse(nino: Nino)(implicit hc: HeaderCarrier): Future[SPResponseModel] = {
    val futureNpsSummary = nps.connectToSummary(nino)
    val futureNpsNIRecord = nps.connectToNIRecord(nino)
    val futureNpsLiabilities = nps.connectToLiabilities(nino)
    val futureNpsSchemeMembership = nps.connectToSchemeMembership(nino)
    val futureManualCorrespondenceIndicator = citizenDetailsService.retrieveMCIStatus(nino)

    for (npsSummary <- futureNpsSummary;
         npsNIRecord <- futureNpsNIRecord;
         npsLiabilities <- futureNpsLiabilities;
         npsSchemeMembership <- futureNpsSchemeMembership;
         manualCorrespondenceIndicator <- futureManualCorrespondenceIndicator) yield {

      val currentAmountReceived  = npsSummary.npsStatePensionAmount.nspEntitlement
      val startingAmountReceived = npsSummary.npsStatePensionAmount.startingAmount2016

      val purgedNIRecord = npsNIRecord.purge(npsSummary.finalRelevantYear)

      val exclusionsService = ExclusionsService(
        npsSummary.isAbroad,
        npsSummary.rreToConsider == 1,
        npsSummary.dateOfDeath,
        npsLiabilities,
        currentAmountReceived,
        startingAmountReceived,
        SPCurrentAmountService.calculate(npsSummary.npsStatePensionAmount.npsAmountA2016, npsSummary.npsStatePensionAmount.npsAmountB2016),
        NpsDate(now),
        npsSummary.spaDate,
        npsSummary.sex,
        manualCorrespondenceIndicator
      )

      val spExclusions = exclusionsService.getSPExclusions
      val niExclusions = exclusionsService.getNIExclusions

      val sanitisedCurrentAmount: BigDecimal =
        if(npsSummary.nspQualifyingYears < NISPConstants.newStatePensionMinimumQualifyingYears) 0
        else currentAmountReceived


      val forecast: SPForecastModel = forecastingService.getForecastAmount(
        npsSchemeMembership, npsSummary.earningsIncludedUpTo, npsSummary.nspQualifyingYears, npsSummary.npsStatePensionAmount.npsAmountA2016,
        npsSummary.npsStatePensionAmount.npsAmountB2016,
        purgedNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).map(_.primaryPaidEarnings).getOrElse(0),
        npsSummary.finalRelevantYear, npsSummary.pensionForecast.forecastAmount, npsSummary.pensionForecast.forecastAmount2016,
        purgedNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).exists(_.qualifying), nino,
        npsNIRecord.nonQualifyingYearsPayable, SPAmountModel(currentAmountReceived)
      )

      val mqpScenario = ForecastingService.getMqpScenario(npsSummary.nspQualifyingYears, npsSummary.yearsUntilPensionAge,
                      npsNIRecord.nonQualifyingYearsPayable)

      val spSummary = SPSummaryModel(
        npsSummary.nino,
        npsSummary.earningsIncludedUpTo,
        SPAmountModel(sanitisedCurrentAmount),
        SPAgeModel(new Period(npsSummary.dateOfBirth.localDate, npsSummary.spaDate.localDate).getYears, npsSummary.spaDate),
        npsSummary.finalRelevantYear,
        purgedNIRecord.numberOfQualifyingYears,
        purgedNIRecord.nonQualifyingYears,
        purgedNIRecord.nonQualifyingYearsPayable,
        npsSummary.yearsUntilPensionAge,
        npsSummary.pensionShareOrderSERPS != 0,
        npsSummary.dateOfBirth,
        forecast,
        npsSummary.pensionForecast.fullNewStatePensionAmount,
        npsSummary.npsStatePensionAmount.npsAmountB2016.rebateDerivedAmount > 0,
        getAge(npsSummary.dateOfBirth),
        SPAmountModel(npsSummary.npsStatePensionAmount.npsAmountB2016.rebateDerivedAmount),
        mqpScenario,
        npsSummary.isAbroad
      )

      if (spExclusions.exclusions.isEmpty && niExclusions.exclusions.isEmpty) {
        metrics.summary(forecast.forecastAmount.week, sanitisedCurrentAmount, npsSchemeMembership.nonEmpty,
          forecast.scenario.equals(Scenario.ForecastOnly), getAge(npsSummary.dateOfBirth), forecast.scenario,
          forecast.personalMaximum.week, forecast.yearsLeftToWork, mqpScenario)
        SPResponseModel(Some(spSummary), None, None)
      } else {
        metrics.exclusion(spExclusions.exclusions)
        SPResponseModel(Some(spSummary), Some(spExclusions), Some(niExclusions))
      }

    }
  }

  def getAge(dateOfBirth: NpsDate): Int =  {
    new Period(dateOfBirth.localDate, now, PeriodType.yearMonthDay()).getYears
  }
}
