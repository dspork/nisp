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
import uk.gov.hmrc.nisp.models.enums.SPContextMessage.SPContextMessage
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.models.{SPAgeModel, SPAmountModel, SPResponseModel, SPSummaryModel}
import uk.gov.hmrc.nisp.utils.WithCurrentDate
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

object SPResponseService extends SPResponseService {
  override val nps: NpsConnector = NpsConnector
  override def now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
  override val forecastingService: ForecastingService = ForecastingService
  override val metrics: Metrics = Metrics
}

trait SPResponseService extends WithCurrentDate {
  val forecastingService: ForecastingService
  val nps: NpsConnector
  val metrics: Metrics

  def getSPResponse(nino: Nino)(implicit hc: HeaderCarrier): Future[SPResponseModel] = {
    val futureNpsSummary = nps.connectToSummary(nino)
    val futureNpsNIRecord = nps.connectToNIRecord(nino)
    val futureNpsLiabilities = nps.connectToLiabilities(nino)
    val futureNpsSchemeMembership = nps.connectToSchemeMembership(nino)

    for (npsSummary <- futureNpsSummary;
         npsNIRecord <- futureNpsNIRecord;
         npsLiabilities <- futureNpsLiabilities;
         npsSchemeMembership <- futureNpsSchemeMembership) yield {
      val spAmountModel = SPAmountModel(npsSummary.npsStatePensionAmount.nspEntitlement)

      val spExclusions = SPExclusionsService(
        npsSummary.nspQualifyingYears,
        npsSummary.countryCode,
        npsSummary.rreToConsider == 1,
        npsSummary.sex,
        npsSchemeMembership,
        npsSummary.dateOfDeath,
        npsSummary.nino,
        npsLiabilities,
        spAmountModel.week,
        SPCurrentAmountService.calculate(npsSummary.npsStatePensionAmount.npsAmountA2016, npsSummary.npsStatePensionAmount.npsAmountB2016),
        NpsDate(now),
        npsSummary.spaDate
      ).getSPExclusions

      if (spExclusions.spExclusions.nonEmpty) {
        metrics.exclusion(spExclusions.spExclusions)
        SPResponseModel(None, Some(spExclusions))
      } else {
        val forecastAmount: SPAmountModel = forecastingService.getForecastAmount(
          npsSchemeMembership, npsSummary.earningsIncludedUpTo, npsSummary.nspQualifyingYears, npsSummary.npsStatePensionAmount.npsAmountA2016,
          npsSummary.npsStatePensionAmount.npsAmountB2016,
          npsNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).map(_.primaryPaidEarnings).getOrElse(0),
          npsSummary.finalRelevantYear, npsSummary.pensionForecast.forecastAmount, npsSummary.pensionForecast.forecastAmount2016,
          npsNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).exists(_.qualifying), nino
        )

        val scenario: Option[SPContextMessage] = SPContextMessageService.getSPContextMessage(
          spAmountModel,
          npsSummary.nspQualifyingYears,
          npsSummary.earningsIncludedUpTo,
          npsNIRecord.nonQualifyingYearsPayable
        )

        metrics.summary(forecastAmount.week, spAmountModel.week, scenario, npsSchemeMembership.nonEmpty,
          spAmountModel.week > forecastAmount.week, getAge(npsSummary.dateOfBirth))

        SPResponseModel(
          Some(SPSummaryModel(
            npsSummary.nino,
            npsSummary.earningsIncludedUpTo,
            spAmountModel,
            SPAgeModel(new Period(npsSummary.dateOfBirth.localDate, npsSummary.spaDate.localDate).getYears, npsSummary.spaDate),
            scenario,
            npsSummary.finalRelevantYear,
            npsNIRecord.numberOfQualifyingYears,
            npsNIRecord.nonQualifyingYears,
            npsNIRecord.nonQualifyingYearsPayable,
            npsSummary.yearsUntilPensionAge,
            npsSummary.pensionShareOrderCOEG != 0 || npsSummary.pensionShareOrderSERPS != 0,
            npsSummary.dateOfBirth,
            forecastAmount,
            npsSummary.pensionForecast.fullNewStatePensionAmount,
            npsSchemeMembership.nonEmpty,
            spAmountModel.week > forecastAmount.week,
            getAge(npsSummary.dateOfBirth),
            SPAmountModel(npsSummary.npsStatePensionAmount.npsAmountB2016.rebateDerivedAmount)
          ))
        )
      }
    }
  }

  def getAge(dateOfBirth: NpsDate): Int =  {
    new Period(dateOfBirth.localDate, now, PeriodType.yearMonthDay()).getYears
  }
}
