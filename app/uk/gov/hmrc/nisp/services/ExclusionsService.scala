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

import play.Logger
import uk.gov.hmrc.nisp.models.ExclusionsModel
import uk.gov.hmrc.nisp.models.enums.Exclusion
import uk.gov.hmrc.nisp.models.enums.Exclusion.Exclusion
import uk.gov.hmrc.nisp.models.nps.{NpsDate, NpsLiability, NpsSchemeMembership}
import uk.gov.hmrc.nisp.utils.{FunctionHelper, NISPConstants}

case class ExclusionsService(isAbroad: Boolean, mwrre: Boolean, dateOfDeath: Option[NpsDate], nino: String,
                             liabilities: List[NpsLiability], currentAmountReceived: BigDecimal,
                             currentAmountCalculated: BigDecimal, now: NpsDate, statePensionAge: NpsDate, sex: String) {

  def getSPExclusions: ExclusionsModel = calculateExclusions(spExclusions)
  def getNIExclusions: ExclusionsModel = calculateExclusions(niExclusions)

  private def calculateExclusions(rules: List[Exclusion] => List[Exclusion]): ExclusionsModel  = {
    val exclusions = rules(List())
    if (exclusions.nonEmpty) {
      val formattedExclusions = exclusions.map(_.toString).mkString(",")
      Logger.info(s"User excluded: $formattedExclusions")
    }

    ExclusionsModel(exclusions)
  }

  val checkAbroad = (exclusionList: List[Exclusion]) =>
    if(isAbroad && statePensionAge.localDate.isBefore(NISPConstants.autoCreditExclusionDate) && sex.equalsIgnoreCase("M"))
      Exclusion.Abroad :: exclusionList
    else
      exclusionList

  val checkIOMLiabilities = (exclusionList: List[Exclusion]) => {
    if (liabilities.exists(_.liabilityType == NISPConstants.isleOfManLiability))
      Exclusion.IOM :: exclusionList
    else
      exclusionList
  }

  val checkMWRRE = (exclusionList: List[Exclusion]) =>
    if (mwrre) Exclusion.MWRRE :: exclusionList else exclusionList

  val checkDead = (exclusionList: List[Exclusion]) =>
    dateOfDeath.fold(exclusionList)(_ => Exclusion.Dead :: exclusionList)

  val checkAmountDissonance = (exclusionList: List[Exclusion]) => {
    if(currentAmountCalculated != currentAmountReceived) {
      Logger.warn(s"Dissonance Found!: nSP Calc - $currentAmountReceived Breakdown - $currentAmountCalculated NINO - $nino")
      Exclusion.AmountDissonance :: exclusionList
    } else {
      exclusionList
    }
  }

  val checkStatePensionAge = (exclusionList: List[Exclusion]) => {
    if(statePensionAge.localDate.isBefore(NISPConstants.newStatePensionStart)) {
      List(Exclusion.CustomerTooOld)
    } else {
      if(!now.localDate.isBefore(statePensionAge.localDate.minusDays(1))) {
        Exclusion.PostStatePensionAge :: exclusionList
      } else {
        exclusionList
      }
    }
  }

  val spExclusions = FunctionHelper.composeAll(List(checkStatePensionAge, checkAbroad, checkMWRRE, checkDead, checkIOMLiabilities, checkAmountDissonance))
  val niExclusions = FunctionHelper.composeAll(List(checkMWRRE, checkDead, checkIOMLiabilities))
}
