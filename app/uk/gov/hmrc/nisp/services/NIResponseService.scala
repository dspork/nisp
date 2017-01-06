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

import org.joda.time.{DateTimeZone, LocalDate}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.nps.{NpsLiability, NpsDate, NpsNITaxYear}
import uk.gov.hmrc.nisp.models._
import uk.gov.hmrc.nisp.services.reference.QualifyingYearsAmountService
import uk.gov.hmrc.nisp.utils.{NISPConstants, WithCurrentDate}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

object NIResponseService extends NIResponseService {
  override val nps: NpsConnector = NpsConnector
  override val metrics: Metrics = Metrics
  override val citizenDetailsService: CitizenDetailsService = CitizenDetailsService

  override def now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
}

trait NIResponseService extends WithCurrentDate {
  val nps: NpsConnector
  val metrics: Metrics
  val citizenDetailsService: CitizenDetailsService

  def getNIResponse(nino: Nino)(implicit hc: HeaderCarrier): Future[NIResponse] = {
    val npsNIRecordFuture = nps.connectToNIRecord(nino)
    val npsSummaryFuture = nps.connectToSummary(nino)
    val npsLiabilitiesFuture = nps.connectToLiabilities(nino)
    val manualCorrespondenceIndicatorFuture = citizenDetailsService.retrieveMCIStatus(nino)

    for (
      npsNIRecord <- npsNIRecordFuture;
      npsSummary <- npsSummaryFuture;
      npsLiabilities <- npsLiabilitiesFuture;
      manualCorrespondenceIndicator <- manualCorrespondenceIndicatorFuture
    ) yield {

      val purgedNIRecord = npsNIRecord.purge(npsSummary.finalRelevantYear)

      val niExclusions = ExclusionsService(
        npsSummary.isAbroad,
        npsSummary.rreToConsider == 1,
        npsSummary.dateOfDeath,
        npsSummary.nino,
        npsLiabilities,
        npsSummary.npsStatePensionAmount.nspEntitlement,
        SPCurrentAmountService.calculate(npsSummary.npsStatePensionAmount.npsAmountA2016, npsSummary.npsStatePensionAmount.npsAmountB2016),
        NpsDate(now),
        npsSummary.spaDate,
        npsSummary.sex,
        manualCorrespondenceIndicator
      ).getNIExclusions

      if (niExclusions.exclusions.nonEmpty) {
        NIResponse(None, None, Some(niExclusions))
      } else {

        val niSummary = NISummary(
          purgedNIRecord.numberOfQualifyingYears,
          purgedNIRecord.nonQualifyingYears,
          npsSummary.yearsUntilPensionAge,
          npsSummary.spaDate.localDate.getYear,
          npsSummary.earningsIncludedUpTo,
          npsSummary.earningsIncludedUpTo.taxYear + 1,
          calcPre75QualifyingYears(purgedNIRecord.pre75ContributionCount, purgedNIRecord.dateOfEntry, npsSummary.dateOfBirth),
          purgedNIRecord.nonQualifyingYearsPayable,
          purgedNIRecord.nonQualifyingYears - purgedNIRecord.nonQualifyingYearsPayable,
          npsSummary.npsStatePensionAmount.nspEntitlement < QualifyingYearsAmountService.maxAmount,
          npsSummary.isAbroad,
          if(npsSummary.yearsUntilPensionAge <= 0) Some(npsSummary.finalRelevantYear) else None,
          npsSummary.finalRelevantYear,
          homeResponsibilitiesProtection(npsLiabilities)
        )

        metrics.niRecord(niSummary.noOfNonQualifyingYears, niSummary.numberOfPayableGaps, niSummary.pre75QualifyingYears.getOrElse(0),
          niSummary.noOfQualifyingYears, Some(niSummary.yearsToContributeUntilPensionAge))

        NIResponse(
          Some(NIRecord(mapNpsTaxYearsToNisp(purgedNIRecord.niTaxYears))),
          Some(niSummary)
        )
      }
    }
  }

  def mapNpsTaxYearsToNisp(npsNITaxYears: List[NpsNITaxYear]): List[NIRecordTaxYear] = {
    npsNITaxYears map { npsNITaxYear =>
      NIRecordTaxYear(
        npsNITaxYear.taxYear,
        npsNITaxYear.qualifying,
        npsNITaxYear.niEarningsEmployed,
        npsNITaxYear.selfEmployedCredits,
        npsNITaxYear.voluntaryCredits,
        npsNITaxYear.otherCredits.foldRight(0)(_.numberOfCredits + _),
        npsNITaxYear.classThreePayable,
        npsNITaxYear.classThreePayableBy,
        npsNITaxYear.classThreePayableByPenalty,
        npsNITaxYear.payable,
        npsNITaxYear.underInvestigation
      )
    }
  }

  def calcPre75QualifyingYears(pre75Contributions: Int, dateOfEntry: NpsDate, dateOfBirth: NpsDate): Option[Int] = {
    val yearCalc: BigDecimal = BigDecimal(pre75Contributions)/50
    val sixteenthBirthday: NpsDate = NpsDate(dateOfBirth.localDate.plusYears(NISPConstants.niRecordMinAge))
    val yearsPre75 = (NISPConstants.niRecordStart - dateOfEntry.taxYear).min(NISPConstants.niRecordStart - sixteenthBirthday.taxYear)
    if (yearsPre75 > 0) {
      Some(yearCalc.setScale(0, BigDecimal.RoundingMode.CEILING).min(yearsPre75).toInt)
    } else {
      None
    }
  }

  def homeResponsibilitiesProtection(liabilities: List[NpsLiability]): Boolean =
    liabilities.exists(liability => NISPConstants.homeResponsibilitiesProtectionTypes.contains(liability.liabilityType))
}
