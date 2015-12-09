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

import uk.gov.hmrc.play.test.UnitSpec

import scala.math.BigDecimal.RoundingMode

class QualifyingYearsAmountServiceSpec extends UnitSpec {
  "getNspAmount called" should {
    "return None for no years" in {
      QualifyingYearsAmountService.getNspAmount(0) shouldBe 0
    }

    "return the maximum amount for a high number" in {
      QualifyingYearsAmountService.getNspAmount(100) shouldBe 151.25
    }

    "return the maximum amount for 35" in {
      QualifyingYearsAmountService.getNspAmount(35) shouldBe 151.25
    }

    "22 Qualifying years should return £95.07" in {
      QualifyingYearsAmountService.getNspAmount(22) shouldBe 95.07
    }

    "17 Qualifying years should return £73.46" in {
      QualifyingYearsAmountService.getNspAmount(17) shouldBe 73.46
    }
  }

  "getBspAmount called" should {
    "return none for no years" in {
      QualifyingYearsAmountService.getBspAmount(0) shouldBe 0
    }

    "return 115.95 for 30 years" in {
      QualifyingYearsAmountService.getBspAmount(30) shouldBe 115.95
    }

    "return 115.95 for 31 years" in {
      QualifyingYearsAmountService.getBspAmount(31) shouldBe 115.95
    }

    "return 96.63 for 25 years" in {
      QualifyingYearsAmountService.getBspAmount(25) shouldBe 96.63
    }

    "return 85.03 for 22 years" in {
      QualifyingYearsAmountService.getBspAmount(22) shouldBe 85.03
    }

    "return 38.65 for 10 years" in {
      QualifyingYearsAmountService.getBspAmount(10) shouldBe 38.65
    }

    "return 3.87 for 1 year" in {
      QualifyingYearsAmountService.getBspAmount(1) shouldBe 3.87
    }
  }

  "nSPAmountPerYear" should {
    "return 4.32 (Maximum Amount - 151.25 divided by Maximum Years - 35" in {
      QualifyingYearsAmountService.nSPAmountPerYear.setScale(8, RoundingMode.HALF_UP) shouldBe BigDecimal(4.32142857)
    }
  }
}
