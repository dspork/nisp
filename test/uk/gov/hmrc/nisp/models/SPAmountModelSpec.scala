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

package uk.gov.hmrc.nisp.models

import uk.gov.hmrc.play.test.UnitSpec

import scala.math.BigDecimal.RoundingMode

class SPAmountModelSpec extends UnitSpec {
  "Weekly / Monthly / Yearly Calculation" should {

    "return 151.25, 657.67, 7892.01" in {
      SPAmountModel(151.25).month shouldBe 657.67
      SPAmountModel(151.25).year shouldBe 7892.01
    }

    "return 43.21, 187.89, 2254.64" in {
      SPAmountModel(43.21).month shouldBe 187.89
      SPAmountModel(43.21).year shouldBe 2254.64
    }

    "return 95.07, 413.38, 4960.62" in {
      SPAmountModel(95.07).month shouldBe 413.38
      SPAmountModel(95.07).year shouldBe 4960.62
    }
  }
}
