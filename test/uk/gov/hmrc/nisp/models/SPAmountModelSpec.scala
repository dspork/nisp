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

package uk.gov.hmrc.nisp.models

import uk.gov.hmrc.play.test.UnitSpec

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

  "SPService Amount Calculation" should {

    "getWeeklyAmount" should {
      "return 0 for 9 qualifying years" in {
        SPAmountModel.getWeeklyAmount(9, 0) shouldBe SPAmountModel(0)
      }
    }

    "getWeeklyAmountRaw" should {
      "return 0 for 9 qualifying years" in {
        SPAmountModel.getWeeklyAmountRaw(9, 0) shouldBe SPAmountModel(38.89)
      }
    }

    "Calculation A" should {
      "return a value" in {
        SPAmountModel.getAmountA(35, 0) shouldBe 115.95
      }

      "return 34.79 for 9 Qualifying Years" in {
        SPAmountModel.getAmountA(9, 0) shouldBe 34.79
      }

      "return 0 for 0 Qualifying Years" in {
        SPAmountModel.getAmountA(0, 0) shouldBe 0
      }

      "return correct summation of cash values" in {
        SPAmountModel.getAmountA(30, 20 + 300 + 4000 + 50000) shouldBe 54435.95
      }
    }

    "calculation B" should {

      "return a value" in {
        SPAmountModel.getAmountB(35) should not be None
      }

      "return 151.25 for 35 Qualifying Years" in {
        SPAmountModel.getAmountB(35) shouldBe 151.25
      }

      "return 151.25 for 36 Qualifying Years" in {
        SPAmountModel.getAmountB(36) shouldBe 151.25
      }

      "return 77.79 for 18 Qualifying Years" in {
        SPAmountModel.getAmountB(18) shouldBe 77.79
      }

      "return 43.21 for 10 Qualifying Years" in {
        SPAmountModel.getAmountB(10) shouldBe 43.21
      }

      "return 38.89 for 9 Qualifying Years" in {
        SPAmountModel.getAmountB(9) shouldBe 38.89
      }

      "return 0 for 0 Qualifying Years" in {
        SPAmountModel.getAmountB(0) shouldBe 0
      }

    }

    "calculation AB comparison" should {

      "return 151.25 for 35 Qualifying Years when Calc A is 0" in {
        SPAmountModel.getWeeklyAmount(35, 0).week shouldBe 151.25
      }

      "return 151.25 for 35 Qualifying Years when Calc A is 151.24 (115.95 + 35.29)" in {
        SPAmountModel.getAmountA(35, 35.29) shouldBe 151.24
        SPAmountModel.getWeeklyAmount(35, 35.29).week shouldBe 151.25
      }

      "return 151.25 for 35 Qualifying Years when Calc A is 151.25" in {
        SPAmountModel.getAmountA(35, 35.30) shouldBe 151.25
        SPAmountModel.getWeeklyAmount(35, 35.30).week shouldBe 151.25
      }

      "return 151.26 for 35 Qualifying Years when Calc A is 151.26" in {
        SPAmountModel.getAmountA(35, 35.31) shouldBe 151.26
        SPAmountModel.getAmountB(35) shouldBe 151.25
        SPAmountModel.getWeeklyAmount(35, 35.31).week shouldBe 151.26
      }

    }

    "amount calculation for DOB from  6/4/1960 to 5/4/1969" should {

      "return amount without factor DOB 6/4/1960" in {
        SPAmountModel.getWeeklyAmount(30, 20+30+40).week shouldBe 205.95
      }

      "return amount without factor DOB 5/4/1960" in {
        SPAmountModel.getWeeklyAmount(30, 20+30+40).week shouldBe 205.95
      }

      "return amount without factor DOB 6/4/1969" in {
        SPAmountModel.getWeeklyAmount(30, 20+30+40).week shouldBe 205.95
      }

      "return amount without factor DOB 5/4/1969" in {
        SPAmountModel.getWeeklyAmount(30, 20+30+40).week shouldBe 205.95
      }

      "return amount without factor DOB 6/4/1961" in {
        SPAmountModel.getWeeklyAmount(30, 20+30+40).week shouldBe 205.95
      }

    }
  }
}
