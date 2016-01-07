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

package uk.gov.hmrc.nisp.models.nps

import uk.gov.hmrc.play.test.UnitSpec

class NpsLiabilitySpec extends UnitSpec {
  "weeksLiableForTaxYear" should {
    "return 53 for taxYear 2014, liability with dates 01/01/2000, 01/01/2016" in {
      NpsLiability(0, Some(NpsDate(2000,1,1)), Some(NpsDate(2016,1,1))).weeksLiableForTaxYear(2014) shouldBe 53
    }

    "return 52 for taxYear 2013, liability with dates 01/01/2000, 01/01/2016" in {
      NpsLiability(0, Some(NpsDate(2000,1,1)), Some(NpsDate(2016,1,1))).weeksLiableForTaxYear(2013) shouldBe 52
    }

    "return 53 for taxYear 2014, liability with dates 06/04/2014, 01/01/2016" in {
      NpsLiability(0, Some(NpsDate(2014,4,6)), Some(NpsDate(2016,1,1))).weeksLiableForTaxYear(2014) shouldBe 53
    }

    "return 52 for taxYear 2013, liability with dates 01/01/2000, 05/04/2014" in {
      NpsLiability(0, Some(NpsDate(2000,1,1)), Some(NpsDate(2015,4,5))).weeksLiableForTaxYear(2013) shouldBe 52
    }

    "return 0 for taxYear 2014, liability with dates 09/03/2014, 29/03/2014" in {
      NpsLiability(0, Some(NpsDate(2014,3,9)), Some(NpsDate(2014,3,29))).weeksLiableForTaxYear(2014) shouldBe 0
    }

    "return 0 for taxYear 2014, liability with dates 09/03/2000, 29/03/2000" in {
      NpsLiability(0, Some(NpsDate(2000,3,9)), Some(NpsDate(2000,3,29))).weeksLiableForTaxYear(2014) shouldBe 0
    }

    "return 0 for taxYear 2012, liability with dates 09/03/2014, 29/03/2014" in {
      NpsLiability(0, Some(NpsDate(2014,3,9)), Some(NpsDate(2014,3,29))).weeksLiableForTaxYear(2012) shouldBe 0
    }

    "return 0 for taxYear 2014, liability with dates 23/03/2014, 05/04/2014" in {
      NpsLiability(0, Some(NpsDate(2014,3,23)), Some(NpsDate(2014,4,5))).weeksLiableForTaxYear(2014) shouldBe 0
    }

    "return 1 for taxYear 2014, liability with dates 22/03/2015, 28/03/2015" in {
      NpsLiability(0, Some(NpsDate(2015,3,22)), Some(NpsDate(2015,3,28))).weeksLiableForTaxYear(2014) shouldBe 1
    }

    "return 0 for taxYear 2015, liability with dates 22/03/2015, 28/03/2015" in {
      NpsLiability(0, Some(NpsDate(2015,3,22)), Some(NpsDate(2015,3,28))).weeksLiableForTaxYear(2015) shouldBe 0
    }

    "return 0 for taxYear 2013, liability with dates 22/03/2015, 28/03/2015" in {
      NpsLiability(0, Some(NpsDate(2015,3,22)), Some(NpsDate(2015,3,28))).weeksLiableForTaxYear(2013) shouldBe 0
    }

    "return 0 for taxYear 2015, liability with dates 05/04/2015, 11/04/2015" in {
      NpsLiability(0, Some(NpsDate(2015,4,5)), Some(NpsDate(2015,4,11))).weeksLiableForTaxYear(2015) shouldBe 0
    }

    "return 1 for taxYear 2014, liability with dates 05/04/2015, 11/04/2015" in {
      NpsLiability(0, Some(NpsDate(2015,4,5)), Some(NpsDate(2015,4,11))).weeksLiableForTaxYear(2014) shouldBe 1
    }

    "return 7 for taxYear 2015, liability with dates 05/07/2015, 22/08/2015" in {
      NpsLiability(0, Some(NpsDate(2015,7,5)), Some(NpsDate(2015,8,22))).weeksLiableForTaxYear(2015) shouldBe 7
    }

    "return 3 for taxYear 2014, liability with dates 22/03/2015, 25/04/2015" in {
      NpsLiability(0, Some(NpsDate(2015,3,22)), Some(NpsDate(2015,4,25))).weeksLiableForTaxYear(2014) shouldBe 3
    }

    "return 2 for taxYear 2015, liability with dates 22/03/2015, 25/04/2015" in {
      NpsLiability(0, Some(NpsDate(2015,3,22)), Some(NpsDate(2015,4,25))).weeksLiableForTaxYear(2015) shouldBe 2
    }
  }

  "totalWeeks" when  {
    "when start date is 08/03/2015 and end date is 04/04/2015" should {
      "return 4" in {
        NpsLiability(13, Some(NpsDate(2015,3,8)), Some(NpsDate(2015,4,4)))
      }
    }
  }
}
