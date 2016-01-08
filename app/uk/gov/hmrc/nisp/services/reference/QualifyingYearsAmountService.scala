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
      0 -> 0, 1 -> 4.45, 2 -> 8.89, 3 -> 13.34, 4 -> 17.79, 5 -> 22.24, 6 -> 26.68, 7 -> 31.13, 8 -> 35.58, 9 -> 40.02,
      10 -> 44.47, 11 -> 48.92, 12 -> 53.37, 13 -> 57.81, 14 -> 62.26, 15 -> 66.71, 16 -> 71.15, 17 -> 75.60,
      18 -> 80.05, 19 -> 84.50, 20 -> 88.94, 21 -> 93.39, 22 -> 97.84, 23 -> 102.28, 24 -> 106.73, 25 -> 111.18,
      26 -> 115.63, 27 -> 120.07, 28 -> 124.52, 29 -> 128.97, 30 -> 133.41, 31 -> 137.86, 32 -> 142.31, 33 -> 146.76,
      34 -> 151.20, 35 -> 155.65
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

