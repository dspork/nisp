/*
 * Copyright 2015 HM Revenue & Customs
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

  private lazy val amountMatrix: Map[Int, BigDecimal] = {
    Map(
      0 -> 0, 1 -> 4.32, 2 -> 8.64, 3 -> 12.96, 4 -> 17.29, 5 -> 21.61, 6 -> 25.93, 7 -> 30.25, 8 -> 34.57, 9 -> 38.89, 10 -> 43.21,
      11 -> 47.54, 12 -> 51.86, 13 -> 56.18, 14 -> 60.5, 15 -> 64.82, 16 -> 69.14, 17 -> 73.46, 18 -> 77.79, 19 -> 82.11, 20 -> 86.43,
      21 -> 90.75, 22 -> 95.07, 23 -> 99.39, 24 -> 103.71, 25 -> 108.04, 26 -> 112.36, 27 -> 116.68, 28 -> 121, 29 -> 125.32, 30 -> 129.64,
      31 -> 133.96, 32 -> 138.29, 33 -> 142.61, 34 -> 146.93, 35 -> 151.25
    )
  }

  lazy val maxYears: Int = amountMatrix.keys.max

  lazy val maxAmount = getNspAmount(maxYears)

  def getNspAmount(totalQualifyingYears: Int): BigDecimal = {
    if (totalQualifyingYears > maxYears) {
      maxAmount
    } else {
      amountMatrix(totalQualifyingYears)
    }
  }

  val bspPerYear: BigDecimal = NISPConstants.maxBasicStatePensionAmount / NISPConstants.maxBasicStatePensionYears

  def getBspAmount(totalQualifyingYears: Int): BigDecimal =
    (bspPerYear * totalQualifyingYears).min(NISPConstants.maxBasicStatePensionAmount).setScale(2, RoundingMode.HALF_UP)

  val nSPAmountPerYear: BigDecimal = maxAmount / maxYears
}

