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

package uk.gov.hmrc.nisp.services.reference

import uk.gov.hmrc.nisp.utils.NISPConstants

import scala.math.BigDecimal.RoundingMode

object QualifyingYearsAmountService {
  // scalastyle:off magic.number

  lazy val maxYears: Int = 35

  lazy val maxAmount: BigDecimal = 155.65

  def getNspAmount(totalQualifyingYears: Int): BigDecimal = {
    if (totalQualifyingYears > maxYears) {
      maxAmount
    } else {
      nSPAmountPerYear * totalQualifyingYears
    }
  }

  val bspPerYear: BigDecimal = NISPConstants.maxBasicStatePensionAmount / NISPConstants.maxBasicStatePensionYears

  def getBspAmount(totalQualifyingYears: Int): BigDecimal =
    (bspPerYear * totalQualifyingYears).min(NISPConstants.maxBasicStatePensionAmount).setScale(2, RoundingMode.HALF_UP)

  val nSPAmountPerYear: BigDecimal = maxAmount / maxYears
}
