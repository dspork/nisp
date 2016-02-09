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
import uk.gov.hmrc.nisp.models.SPExclusionsModel
import uk.gov.hmrc.nisp.models.enums.SPExclusion
import uk.gov.hmrc.nisp.models.enums.SPExclusion.SPExclusion
import uk.gov.hmrc.nisp.models.nps.{NpsDate, NpsLiability, NpsSchemeMembership}
import uk.gov.hmrc.nisp.utils.{FunctionHelper, NISPConstants}

case class SPExclusionsService(numberOfQualifyingYears: Int, countryCode: Int, mwrre: Boolean, sex: String,
                               schemeMemberships: List[NpsSchemeMembership], dateOfDeath: Option[NpsDate], nino: String, liabilities: List[NpsLiability],
                               currentAmountReceived: BigDecimal, currentAmountCalculated: BigDecimal, now: NpsDate, statePensionAge: NpsDate) {

  def getSPExclusions: SPExclusionsModel = {
    val exclusions = allExclusions(List())

    if (exclusions.nonEmpty) {
      val formattedExclusions = exclusions.map(_.toString).mkString(",")
      Logger.info(s"User excluded: $formattedExclusions")
    }

    SPExclusionsModel(exclusions)
  }

  val checkAbroad = (exclusionList: List[SPExclusion]) => {
    countryCode match {
      case NISPConstants.countryNotSpecified => exclusionList
      case NISPConstants.countryGB => exclusionList
      case NISPConstants.countryNI => exclusionList
      case NISPConstants.countryEngland => exclusionList
      case NISPConstants.countryScotland => exclusionList
      case NISPConstants.countryWales => exclusionList
      case NISPConstants.countryIsleOfMan => exclusionList
      case _ => SPExclusion.Abroad :: exclusionList
    }
  }

  val checkIOMLiabilities = (exclusionList: List[SPExclusion]) => {
    if (liabilities.exists(_.liabilityType == NISPConstants.isleOfManLiability))
      SPExclusion.IOM :: exclusionList
    else
      exclusionList
  }

  val checkMWRRE = (exclusionList: List[SPExclusion]) =>
    if (mwrre) SPExclusion.MWRRE :: exclusionList else exclusionList

  val checkDead = (exclusionList: List[SPExclusion]) =>
    dateOfDeath.fold(exclusionList)(_ => SPExclusion.Dead :: exclusionList)

  val checkAmountDissonance = (exclusionList: List[SPExclusion]) => {
    if(currentAmountCalculated != currentAmountReceived) {
      Logger.warn(s"Dissonance Found!: nSP Calc - $currentAmountReceived Breakdown - $currentAmountCalculated")
      SPExclusion.AmountDissonance :: exclusionList
    } else {
      exclusionList
    }
  }

  val checkPostStatePensionAge = (exclusionList: List[SPExclusion]) => {
    if(statePensionAge.localDate.isBefore(NISPConstants.newStatePensionStart)) {
      SPExclusion.CustomerTooOld :: exclusionList
    } else {
      if(!now.localDate.isBefore(statePensionAge.localDate.minusDays(1))) {
        SPExclusion.PostStatePensionAge :: exclusionList
      } else {
        exclusionList
      }
    }
  }

  val allExclusions = FunctionHelper.composeAll(List(checkPostStatePensionAge, checkAbroad, checkMWRRE, checkDead, checkIOMLiabilities, checkAmountDissonance))
}
