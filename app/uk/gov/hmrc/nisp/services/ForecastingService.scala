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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.connectors.CustomAuditConnector
import uk.gov.hmrc.nisp.events.ForecastingEvent
import uk.gov.hmrc.nisp.models.SPAmountModel
import uk.gov.hmrc.nisp.models.nps.{NpsAmountB2016, NpsAmountA2016, NpsSchemeMembership, NpsDate}
import uk.gov.hmrc.nisp.services.reference.{QualifyingYearsAmountService, EarningLevelService}
import uk.gov.hmrc.nisp.utils.NISPConstants
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.math.BigDecimal.RoundingMode

object ForecastingService extends ForecastingService {
  override val customAuditConnector: CustomAuditConnector = CustomAuditConnector
}

trait ForecastingService {

  val customAuditConnector: CustomAuditConnector

  def getForecastAmount(npsSchemeMembership: List[NpsSchemeMembership], earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, amountA: NpsAmountA2016,
                        amountB: NpsAmountB2016, lastYearEarnings: BigDecimal, finalRelevantYear: Int,
                        forecastAmount: BigDecimal, forecastAmount2016: BigDecimal, lastYearQualifying: Boolean, nino: Nino)
                       (implicit hc: HeaderCarrier): SPAmountModel = {

    if (npsSchemeMembership.exists(_.contains(earningsIncludedUpTo))) {
      customAuditConnector.sendEvent(ForecastingEvent(nino, earningsIncludedUpTo, currentQualifyingYears, amountA, amountB, lastYearEarnings, finalRelevantYear,
        forecastAmount, forecastAmount2016, lastYearQualifying, "Contracted out at the end of the last posted tax year"))
      SPAmountModel(ForecastingService.forecast(earningsIncludedUpTo, currentQualifyingYears, amountB.rebateDerivedAmount,
        amountA.totalAP, lastYearEarnings, finalRelevantYear, contractedOutLastYear = true))
    } else {
      customAuditConnector.sendEvent(ForecastingEvent(nino, earningsIncludedUpTo, currentQualifyingYears, amountA, amountB, lastYearEarnings, finalRelevantYear,
        forecastAmount, forecastAmount2016, lastYearQualifying,
        "Customer has been contracted out in the past, but ended the last posted tax year contracted in"))
      SPAmountModel(ForecastingService.forecast(earningsIncludedUpTo, currentQualifyingYears, amountB.rebateDerivedAmount,
        amountA.totalAP, lastYearEarnings, finalRelevantYear, contractedOutLastYear = false))
    }
  }

  def adjustForecast(forecastAmount: BigDecimal, forecastAmount2016: BigDecimal, amountATotal: BigDecimal, amountBTotal: BigDecimal): BigDecimal = {
    if (isProtectedAfterAdjustment(forecastAmount2016))
      NISPConstants.fraa2014 + forecastAmount2016
    else if (isAmountA2016AfterAdjustment(amountATotal, amountBTotal))
      (forecastAmount + NISPConstants.fraa2014).min(QualifyingYearsAmountService.maxAmount)
    else
      forecastAmount
  }

  def isProtectedAfterAdjustment(forecastAmount2016: BigDecimal): Boolean =
    forecastAmount2016 >= QualifyingYearsAmountService.maxAmount - NISPConstants.fraa2014

  def isAmountA2016AfterAdjustment(amountATotal: BigDecimal, amountBTotal: BigDecimal): Boolean = NISPConstants.fraa2014 + amountATotal >= amountBTotal

  def forecast(earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, existingRDA: BigDecimal, existingAP: BigDecimal,
               lastYearEarnings: BigDecimal, finalRelevantYear: Int, contractedOutLastYear: Boolean): BigDecimal = {
    val qysAt2016 = totalQualifyingYearsAt2016(earningsIncludedUpTo, currentQualifyingYears)
    val basicPensionAt2016 = QualifyingYearsAmountService.getBspAmount(qysAt2016)

    val cappedEarnings = cappedEarningsAtUAP(lastYearEarnings)
    val s2pAmountLastYear = s2pAmount(earningsIncludedUpTo, band2(cappedEarnings, earningsIncludedUpTo))
    val s2pDeductionLastYear: BigDecimal =
      if (contractedOutLastYear) s2pDeduction(earningsIncludedUpTo, earningsAboveLEL(cappedEarnings, earningsIncludedUpTo), lastYearEarnings, s2pAmountLastYear)
      else 0.0
    val amountA2016 = forecastAmountAAt2016(basicPensionAt2016, existingAP, forecastAPToAccrue(earningsIncludedUpTo, s2pAmountLastYear - s2pDeductionLastYear))
    val rda2016 = forecastRDAAt2016(earningsIncludedUpTo, s2pDeductionLastYear, existingRDA)
    val amountB2016 = forecastAmountBAt2016(QualifyingYearsAmountService.getNspAmount(qysAt2016), rda2016)

    val startingAmount = amountA2016.max(amountB2016)
    forecastPost2016StatePension(finalRelevantYear, startingAmount, qysAt2016)
  }

  def forecastAmountBAt2016(mainAmount2016: BigDecimal, rebateDerivedAmount2016: BigDecimal): BigDecimal =
    mainAmount2016 - rebateDerivedAmount2016

  def forecastRDAAt2016(earningsIncludedUpTo: NpsDate, lastYearS2PDeduction: BigDecimal, existingRDA: BigDecimal): BigDecimal =
    qualifyingYearsTo2016(earningsIncludedUpTo) * lastYearS2PDeduction + existingRDA

  def s2pDeduction(earningsIncludedUpTo: NpsDate, earningsAboveLEL: BigDecimal, totalEarnings: BigDecimal, s2pAmount: BigDecimal): BigDecimal =
    if (totalEarnings > NISPConstants.upperAccrualPoint)
      s2pAmount
    else
      (earningsAboveLEL / NISPConstants.s2pBand2Divisor1Deduction / NISPConstants.s2pBand2Divisor2 /
        NISPConstants.weeksInYear * EarningLevelService.rdaRate(earningsIncludedUpTo.taxYear)).setScale(2, RoundingMode.HALF_UP)

  def earningsAboveLEL(cappedEarnings: BigDecimal, earningsIncludedUpTo: NpsDate): BigDecimal =
    (cappedEarnings - EarningLevelService.qualifyingLevel(earningsIncludedUpTo.taxYear)).max(0)

  def s2pAmount(earningsIncludedUpTo: NpsDate, band2: BigDecimal): BigDecimal =
    (NISPConstants.fraa2014 + (band2 / NISPConstants.s2pBand2Divisor1 / NISPConstants.s2pBand2Divisor2 /
      NISPConstants.weeksInYear * EarningLevelService.rdaRate(earningsIncludedUpTo.taxYear))).setScale(2, RoundingMode.HALF_UP)

  def band2(cappedEarnings: BigDecimal, earningsIncludedUpTo: NpsDate): BigDecimal =
    (cappedEarnings - EarningLevelService.lowerEarningThreshold(earningsIncludedUpTo.taxYear)).max(0)

  def cappedEarningsAtUAP(earnings: BigDecimal): BigDecimal = earnings.min(NISPConstants.upperAccrualPoint)

  def forecastAmountAAt2016(basicPension2016: BigDecimal, existingAP: BigDecimal, forecastAccruedAP: BigDecimal): BigDecimal =
    basicPension2016 + existingAP + forecastAccruedAP

  def forecastAPToAccrue(earningsIncludedUpTo: NpsDate, lastYearAP: BigDecimal): BigDecimal =
    qualifyingYearsTo2016(earningsIncludedUpTo) * lastYearAP

  def totalQualifyingYearsAt2016(earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int): Int =
    qualifyingYearsTo2016(earningsIncludedUpTo) + currentQualifyingYears

  def qualifyingYearsTo2016(earningsIncludedUpTo: NpsDate): Int = (NISPConstants.newStatePensionStart - 1 - earningsIncludedUpTo.taxYear).max(0)

  def qualifyingYearsToFRY(finalRelevantYear: Int): Int = finalRelevantYear - NISPConstants.newStatePensionStart + 1

  def forecastPost2016StatePension(finalRelevantYear: Int, startingAmount: BigDecimal, qualifyingYearsAt2016: Int): BigDecimal = {
    if(startingAmount >= QualifyingYearsAmountService.maxAmount)
      startingAmount
    else if (qualifyingYearsToFRY(finalRelevantYear) + qualifyingYearsAt2016 < NISPConstants.newStatePensionMinimumQualifyingYears)
      0
    else
      (startingAmount + (qualifyingYearsToFRY(finalRelevantYear) * QualifyingYearsAmountService.nSPAmountPerYear))
        .setScale(2, RoundingMode.HALF_UP)
        .min(QualifyingYearsAmountService.maxAmount)
  }
}
