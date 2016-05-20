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
import uk.gov.hmrc.nisp.models.enums.Scenario
import uk.gov.hmrc.nisp.models.enums.Scenario.Scenario
import uk.gov.hmrc.nisp.models.{Forecast, SPAmountModel, SPForecastModel}
import uk.gov.hmrc.nisp.models.nps.{NpsAmountA2016, NpsAmountB2016, NpsDate, NpsSchemeMembership}
import uk.gov.hmrc.nisp.services.reference.{EarningLevelService, QualifyingYearsAmountService}
import uk.gov.hmrc.nisp.utils.NISPConstants
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.annotation.tailrec
import scala.math.BigDecimal.RoundingMode

object ForecastingService extends ForecastingService {
  override val customAuditConnector: CustomAuditConnector = CustomAuditConnector
}

trait ForecastingService {

  val customAuditConnector: CustomAuditConnector

  def getForecastAmount(npsSchemeMembership: List[NpsSchemeMembership], earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, amountA: NpsAmountA2016,
                        amountB: NpsAmountB2016, lastYearEarnings: BigDecimal, finalRelevantYear: Int,
                        forecastAmount: BigDecimal, forecastAmount2016: BigDecimal, lastYearQualifying: Boolean, nino: Nino, fillableGaps: Int, currentAmount: SPAmountModel)
                       (implicit hc: HeaderCarrier): SPForecastModel = {

    val contractedOutLastYear: Boolean = npsSchemeMembership.exists(_.contains(earningsIncludedUpTo))

    customAuditConnector.sendEvent(ForecastingEvent(nino, earningsIncludedUpTo, currentQualifyingYears, amountA,
      amountB, lastYearEarnings, finalRelevantYear, forecastAmount, forecastAmount2016, lastYearQualifying,
      contractedOutLastYear))

    val calculatedForecast = forecast(earningsIncludedUpTo, currentQualifyingYears,
      amountB.rebateDerivedAmount, amountA.totalAP, lastYearEarnings, finalRelevantYear, contractedOutLastYear)

    val forecastFillingsGaps: Int => BigDecimal = (gapsToFill: Int)  => forecast(earningsIncludedUpTo, currentQualifyingYears + gapsToFill,
      amountB.rebateDerivedAmount, amountA.totalAP, lastYearEarnings, finalRelevantYear, contractedOutLastYear).amount

    val personalMaximumAmount = personalMaximum(fillableGaps, forecastFillingsGaps)

    val scenario = forecastScenario(currentAmount, SPAmountModel(calculatedForecast.amount), personalMaximumAmount,
      currentQualifyingYears + calculatedForecast.yearsLeftToWork + fillableGaps)

    SPForecastModel(
      SPAmountModel(calculatedForecast.amount),
      if(scenario == Scenario.Reached) 0 else calculatedForecast.yearsLeftToWork,
      personalMaximumAmount,
      if(scenario == Scenario.FillGaps) minimumGapsToFillForPersonalMax(personalMaximumAmount.week, fillableGaps, forecastFillingsGaps) else 0,
      scenario,
      calculatedForecast.oldRulesCustomer
    )

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
               lastYearEarnings: BigDecimal, finalRelevantYear: Int, contractedOutLastYear: Boolean): Forecast = {
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

    forecastPost2016StatePension(
      finalRelevantYear,
      startingAmount,
      qysAt2016,
      qualifyingYearsTo2016(earningsIncludedUpTo)
    )
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

  def qualifyingYearsTo2016(earningsIncludedUpTo: NpsDate): Int = (NISPConstants.newStatePensionStartYear - 1 - earningsIncludedUpTo.taxYear).max(0)

  def qualifyingYearsToFRY(finalRelevantYear: Int): Int = finalRelevantYear - NISPConstants.newStatePensionStartYear + 1

  def forecastPost2016StatePension(finalRelevantYear: Int, startingAmount: BigDecimal, qualifyingYearsAt2016: Int,
                                   pre2016YearsToContribute: Int): Forecast = {
    if(startingAmount >= QualifyingYearsAmountService.maxAmount)
      Forecast(startingAmount, pre2016YearsToContribute, oldRulesCustomer = true)
    else if (qualifyingYearsToFRY(finalRelevantYear) + qualifyingYearsAt2016 < NISPConstants.newStatePensionMinimumQualifyingYears)
      Forecast(0, 0, oldRulesCustomer = false)
    else {
      val amountNeeded: BigDecimal = QualifyingYearsAmountService.maxAmount - startingAmount
      val yearsNeeded: Int = (amountNeeded / QualifyingYearsAmountService.nSPAmountPerYear).setScale(0, RoundingMode.CEILING).toInt
      val yearsPossible: Int = yearsNeeded.min(qualifyingYearsToFRY(finalRelevantYear))

      Forecast((startingAmount + (yearsPossible * QualifyingYearsAmountService.nSPAmountPerYear))
        .setScale(2, RoundingMode.HALF_UP)
        .min(QualifyingYearsAmountService.maxAmount),
        yearsPossible + pre2016YearsToContribute,
        oldRulesCustomer = false)
    }

  }

  def forecastScenario(current: SPAmountModel, forecast: SPAmountModel, personalMaximum: SPAmountModel, yearsToQualify: Int): Scenario = {
    if(yearsToQualify < NISPConstants.newStatePensionMinimumQualifyingYears)
      Scenario.CantGetPension
    else if(forecast.week < current.week){
      Scenario.ForecastOnly
    } else (current == forecast, personalMaximum.week > forecast.week) match {
      case (true, false) => Scenario.Reached
      case (_, true) => Scenario.FillGaps
      case (false, false) if forecast.week >= QualifyingYearsAmountService.maxAmount => Scenario.ContinueWorkingMax
      case (false, false) => Scenario.ContinueWorkingNonMax
    }
  }

  def personalMaximum(fillableGaps: Int, forecastWithFilledGaps: Int => BigDecimal): SPAmountModel = {
    SPAmountModel(forecastWithFilledGaps(fillableGaps))
  }

  def minimumGapsToFillForPersonalMax(personalMaximum: BigDecimal, fillableGaps: Int, forecastWithFilledGaps: Int => BigDecimal): Int = {
    require(fillableGaps > 0)

    @tailrec def go(years: Int): Int = {
      if(forecastWithFilledGaps(years) < personalMaximum) years + 1
      else go(years - 1)
    }

    go(fillableGaps)
  }

}
