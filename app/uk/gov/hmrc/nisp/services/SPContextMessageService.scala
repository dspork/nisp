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

import uk.gov.hmrc.nisp.models.SPAmountModel
import uk.gov.hmrc.nisp.models.enums.SPContextMessage
import uk.gov.hmrc.nisp.models.enums.SPContextMessage.SPContextMessage
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.services.reference.QualifyingYearsAmountService
import uk.gov.hmrc.nisp.utils.NISPConstants

object SPContextMessageService {

  def getSPContextMessage(spAmountModel: SPAmountModel,
                          numberOfQualifyingYears: Int,
                          earningsIncludedUpTo: NpsDate,
                          numberOfPayableGaps: Int): Option[SPContextMessage] = {
    (isAmountBRelevant(spAmountModel), isFullBasicStatePension(numberOfQualifyingYears)) match {
      case (false, true) => Some(SPContextMessage.ScenarioOne)
      case (false, false) =>
        if (numberOfPayableGaps > 0) {
          if (timeToFillBasicPensionGaps(earningsIncludedUpTo, numberOfQualifyingYears)) {
            Some(SPContextMessage.ScenarioTwo)
          } else {
            Some(SPContextMessage.ScenarioFour)
          }
        } else {
          Some(SPContextMessage.ScenarioThree)
        }
      case (true, true) =>
        if (numberOfPayableGaps > 0) {
          Some(SPContextMessage.ScenarioSix)
        } else {
          Some(SPContextMessage.ScenarioFive)
        }
      case (true, false) =>
        if (numberOfPayableGaps > 0) {
          Some(SPContextMessage.ScenarioEight)
        } else {
          Some(SPContextMessage.ScenarioSeven)
        }
    }
  }

  private def isAmountBRelevant(spAmount: SPAmountModel): Boolean = spAmount.week < QualifyingYearsAmountService.maxAmount

  private def isFullBasicStatePension(qualifyingYears: Int): Boolean = qualifyingYears >= NISPConstants.fullBasicPensionQualifyingYears

  private def timeToFillBasicPensionGaps(earningsIncludedUpTo: NpsDate, numberOfQualifyingYears: Int): Boolean =
    NISPConstants.newStatePensionStart - earningsIncludedUpTo.taxYear - 1 >= NISPConstants.fullBasicPensionQualifyingYears - numberOfQualifyingYears
}
