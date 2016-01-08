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
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.models.{SPAgeModel, SPAmountModel, SPResponseModel, SPSummaryModel}
import uk.gov.hmrc.nisp.services.reference.QualifyingYearsAmountService
import uk.gov.hmrc.nisp.utils.WithCurrentDate
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait SPResponseService extends WithCurrentDate {
  def nps: NpsConnector

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
      val spExclusionsOption = SPExclusionsService(
        npsSummary.nspQualifyingYears,
        npsSummary.countryCode,
        npsSummary.rreToConsider == 1,
        npsSummary.sex,
        npsSummary.dateOfBirth,
        npsSchemeMembership,
        npsSummary.dateOfDeath,
        npsSummary.nino,
        npsLiabilities
      ).getSPExclusions

      spExclusionsOption match {
        case Some(spExclusions) => SPResponseModel(None, Some(spExclusions))
        case _ => SPResponseModel(
          Some(SPSummaryModel(
            npsSummary.nino,
            npsSummary.earningsIncludedUpTo,
            spAmountModel,
            SPAgeModel(new Period(npsSummary.dateOfBirth.localDate, npsSummary.spaDate.localDate).getYears, npsSummary.spaDate),
            SPContextMessageService.getSPContextMessage(
              spAmountModel,
              npsSummary.nspQualifyingYears,
              npsSummary.earningsIncludedUpTo,
              npsNIRecord.nonQualifyingYearsPayable
            ),
            npsSummary.finalRelevantYear,
            npsNIRecord.numberOfQualifyingYears,
            npsNIRecord.nonQualifyingYears,
            npsNIRecord.nonQualifyingYearsPayable,
            npsSummary.yearsUntilPensionAge,
            npsSummary.pensionShareOrderCOEG != 0 || npsSummary.pensionShareOrderSERPS != 0,
            npsSummary.dateOfBirth,
            ForecastingService.getForecastAmount(
              npsSchemeMembership, npsSummary.earningsIncludedUpTo,
              npsSummary.nspQualifyingYears,
              npsSummary.npsStatePensionAmount.npsAmountA2016,
              npsSummary.npsStatePensionAmount.npsAmountB2016,
              npsNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).map(_.primaryPaidEarnings).getOrElse(0),
              npsSummary.finalRelevantYear,
              npsSummary.pensionForecast.forecastAmount,
              npsSummary.pensionForecast.forecastAmount2016,
              npsNIRecord.niTaxYears.find(_.taxYear == npsSummary.earningsIncludedUpTo.taxYear).exists(_.qualifying),
              nino
            ),
            QualifyingYearsAmountService.maxAmount,
            npsSchemeMembership.nonEmpty,
            getAge(npsSummary.dateOfBirth),
            SPAmountModel(npsSummary.npsStatePensionAmount.npsAmountB2016.rebateDerivedAmount),
            SPAmountModel(npsSummary.pensionForecast.nspMax)
          ))
        )
      }
    }
  }

  def getAge(dateOfBirth: NpsDate): Int =  {
    new Period(dateOfBirth.localDate, now, PeriodType.yearMonthDay()).getYears
  }
}

object SPResponseService extends SPResponseService {
  override val nps: NpsConnector = NpsConnector
  override val now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
}
