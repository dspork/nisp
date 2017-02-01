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
import uk.gov.hmrc.nisp.domain.TaxYear
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.nps.{NpsDate, NpsLiability, NpsNITaxYear}
import uk.gov.hmrc.nisp.models.{NationalInsuranceRecord, NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear}
import uk.gov.hmrc.nisp.utils.{NISPConstants, WithCurrentDate}
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NationalInsuranceRecordService extends WithCurrentDate {
  val npsConnector: NpsConnector
  val metrics: Metrics
  val citizenDetailsService: CitizenDetailsService

  def getTaxYear(nino: Nino, taxYear: TaxYear)(implicit request: HeaderCarrier):
    Future[Either[NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear]] = {

    val npsNIRecordFuture = npsConnector.connectToNIRecord(nino)
    val npsSummaryFuture = npsConnector.connectToSummary(nino)
    val npsLiabilitiesFuture = npsConnector.connectToLiabilities(nino)
    val manualCorrespondenceIndicatorFuture = citizenDetailsService.retrieveMCIStatus(nino)

    for (
      npsNIRecord <- npsNIRecordFuture;
      npsSummary <- npsSummaryFuture;
      npsLiabilities <- npsLiabilitiesFuture;
      manualCorrespondenceIndicator <- manualCorrespondenceIndicatorFuture
    ) yield {

      val purgedNIRecord = npsNIRecord.purge(npsSummary.finalRelevantYear)

      val niExclusions = ExclusionsService( npsSummary.isAbroad,
        npsSummary.rreToConsider == 1,
        npsSummary.dateOfDeath,
        npsLiabilities,
        npsSummary.npsStatePensionAmount.nspEntitlement,
        npsSummary.npsStatePensionAmount.startingAmount2016,
        SPCurrentAmountService.calculate(npsSummary.npsStatePensionAmount.npsAmountA2016, npsSummary.npsStatePensionAmount.npsAmountB2016),
        NpsDate(now),
        npsSummary.spaDate,
        npsSummary.sex,
        manualCorrespondenceIndicator
      ).getNIExclusions

      if (niExclusions.exclusions.nonEmpty) {
        Left(NationalInsuranceRecordExclusion(niExclusions.exclusions))
      } else {

        purgedNIRecord.niTaxYears.map(npsTaxYearToNIRecordTaxYear).find(x => x.taxYear == taxYear.taxYear) match {
          case Some(nationalInsuranceRecordTaxYear) => Right(nationalInsuranceRecordTaxYear)
          case _ => throw new NotFoundException(s"taxYear ${taxYear.taxYear} Not Found for $nino")
        }
      }
    }
  }

  def getSummary(nino: Nino)(implicit request: HeaderCarrier):
  Future[Either[NationalInsuranceRecordExclusion, NationalInsuranceRecord]] = {

    val npsNIRecordFuture = npsConnector.connectToNIRecord(nino)
    val npsSummaryFuture = npsConnector.connectToSummary(nino)
    val npsLiabilitiesFuture = npsConnector.connectToLiabilities(nino)
    val manualCorrespondenceIndicatorFuture = citizenDetailsService.retrieveMCIStatus(nino)

    for (
      npsNIRecord <- npsNIRecordFuture;
      npsSummary <- npsSummaryFuture;
      npsLiabilities <- npsLiabilitiesFuture;
      manualCorrespondenceIndicator <- manualCorrespondenceIndicatorFuture
    ) yield {

      val purgedNIRecord = npsNIRecord.purge(npsSummary.finalRelevantYear)

      val niExclusions = ExclusionsService(npsSummary.isAbroad,
        npsSummary.rreToConsider == 1,
        npsSummary.dateOfDeath,
        npsLiabilities,
        npsSummary.npsStatePensionAmount.nspEntitlement,
        npsSummary.npsStatePensionAmount.startingAmount2016,
        SPCurrentAmountService.calculate(npsSummary.npsStatePensionAmount.npsAmountA2016, npsSummary.npsStatePensionAmount.npsAmountB2016),
        NpsDate(now),
        npsSummary.spaDate,
        npsSummary.sex,
        manualCorrespondenceIndicator
      ).getNIExclusions

      if (niExclusions.exclusions.nonEmpty) {
        Left(NationalInsuranceRecordExclusion(niExclusions.exclusions))
      } else {

        val niRecord = NationalInsuranceRecord(
          purgedNIRecord.numberOfQualifyingYears,
          calcPre75QualifyingYears(purgedNIRecord.pre75ContributionCount, purgedNIRecord.dateOfEntry, npsSummary.dateOfBirth).getOrElse(0),
          purgedNIRecord.nonQualifyingYears,
          purgedNIRecord.nonQualifyingYearsPayable,
          purgedNIRecord.dateOfEntry.localDate,
          homeResponsibilitiesProtection(npsLiabilities),
          npsSummary.earningsIncludedUpTo.localDate,
          purgedNIRecord.niTaxYears.map(npsTaxYearToNIRecordTaxYear).sortBy(_.taxYear)(Ordering[String].reverse)
        )

        metrics.niRecord(niRecord.numberOfGaps, niRecord.numberOfGapsPayable, niRecord.qualifyingYearsPriorTo1975,
          niRecord.qualifyingYears, None)

        Right(niRecord)
      }
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

  def npsTaxYearToNIRecordTaxYear(npsNITaxYear: NpsNITaxYear): NationalInsuranceRecordTaxYear = {
      NationalInsuranceRecordTaxYear(
        TaxYear.getFormattedTaxYear(npsNITaxYear.taxYear).value,
        npsNITaxYear.qualifying,
        npsNITaxYear.niEarningsEmployed,
        npsNITaxYear.selfEmployedCredits,
        npsNITaxYear.voluntaryCredits,
        npsNITaxYear.otherCredits.foldRight(0)(_.numberOfCredits + _),
        npsNITaxYear.classThreePayable.getOrElse(0),
        npsNITaxYear.classThreePayableBy.map(_.localDate),
        npsNITaxYear.classThreePayableByPenalty.map(_.localDate),
        npsNITaxYear.payable,
        npsNITaxYear.underInvestigation
      )
  }
}

object NationalInsuranceRecordService extends NationalInsuranceRecordService {
  override val npsConnector: NpsConnector = NpsConnector
  override val metrics: Metrics = Metrics
  override val citizenDetailsService: CitizenDetailsService = CitizenDetailsService
  override def now: LocalDate = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")))
}
