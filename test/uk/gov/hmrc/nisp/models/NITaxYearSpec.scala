/*
 * Copyright 2017 HM Revenue & Customs
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

class NITaxYearSpec extends UnitSpec {
  "shortfall calculation" should {
    "return 0 for 2014 tax year with £40000 earnings" in {
      NITaxYear(2014,40000,0,0,0,0,0).shortfall shouldBe 0
    }

    "return 0 for 2014 tax year with £5772 earnings" in {
      NITaxYear(2014,5772,0,0,0,0,0).shortfall shouldBe 0
    }

    "return 0.01 for 2014 tax year with £5771.99 earnings" in {
      NITaxYear(2014,5771.99,0,0,0,0,0).shortfall shouldBe 0.01
    }

    "return 5772 for 2014 tax year with £0 earnings" in {
      NITaxYear(2014,0,0,0,0,0,0).shortfall shouldBe 5772
    }
  }

  "isQualifying" should {
    "return true for 2014 tax year with £5772 earnings" in {
      NITaxYear(2014,5772,0,0,0,0,0).isQualifying shouldBe true
    }

    "return false for 2014 tax year with £5771.99 earnings" in {
      NITaxYear(2014,5771.99,0,0,0,0,0).isQualifying shouldBe false
    }
  }

  "shortfallCanBeFilledWithUnpaidClassTwoCredits" should {
    "return true for 2014 tax year with 1 unpaid credit and 5661 earnings" in {
      NITaxYear(2014,5661,1,0,0,0,0).shortfallCanBeFilledWithUnpaidClassTwoCredits shouldBe true
    }

    "return false for 2014 tax yar with 1 unpaid credit and 5660.99 earnings" in {
      NITaxYear(2014,5660.99,1,0,0,0,0).shortfallCanBeFilledWithUnpaidClassTwoCredits shouldBe false
    }

    "return true for 2014 tax year with 2 unpaid credits and 5550 earnings" in {
      NITaxYear(2014,5550,2,0,0,0,0).shortfallCanBeFilledWithUnpaidClassTwoCredits shouldBe true
    }

    "return false for 2014 tax year with 2 unpaid credits and 5549.99 earnings" in {
      NITaxYear(2014,5549.99,2,0,0,0,0).shortfallCanBeFilledWithUnpaidClassTwoCredits shouldBe false
    }
  }
}
