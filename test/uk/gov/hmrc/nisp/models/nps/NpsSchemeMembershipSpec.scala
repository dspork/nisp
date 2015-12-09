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

package uk.gov.hmrc.nisp.models.nps

import uk.gov.hmrc.play.test.UnitSpec

class NpsSchemeMembershipSpec extends UnitSpec {
  "contains" should {
    "return true for Start: None and End: None" in {
      NpsSchemeMembership(None, None).contains(NpsDate(2000, 1, 1)) shouldBe true
    }

    "return false for Start: 01/01/2016 and End: None and Date is 01/01/2000" in {
      NpsSchemeMembership(Some(NpsDate(2016, 1, 1)), None).contains(NpsDate(2000, 1, 1)) shouldBe false
    }

    "return true for Start: 01/01/2016 and End: None and Date is 01/01/2013" in {
      NpsSchemeMembership(Some(NpsDate(2016, 1, 1)), None).contains(NpsDate(2013, 1, 1)) shouldBe false
    }

    "return true for Start: None and End: 01/01/2016 and Date is 01/01/2000" in {
      NpsSchemeMembership(None, Some(NpsDate(2016, 1, 1))).contains(NpsDate(2000, 1, 1)) shouldBe true
    }

    "return false for Start: None and End: 01/01/2016 and Date is 01/01/2018" in {
      NpsSchemeMembership(None, Some(NpsDate(2016, 1, 1))).contains(NpsDate(2018, 1, 1)) shouldBe false
    }

    "return true for Start: 01/01/2015 and End: 01/01/2016 and Date is 01/01/2016" in {
      NpsSchemeMembership(Some(NpsDate(2015, 1, 1)), Some(NpsDate(2016, 1, 1))).contains(NpsDate(2016, 1, 1)) shouldBe true
    }

    "return false for Start: 01/01/2015 and End: 01/01/2016 and Date is 02/01/2016" in {
      NpsSchemeMembership(Some(NpsDate(2015, 1, 1)), Some(NpsDate(2016, 1, 1))).contains(NpsDate(2016, 1, 2)) shouldBe false
    }
  }

  "exists in tax year" should {
    "return true for Start: None and End: None" in {
      NpsSchemeMembership(None, None).existsInTaxYear(2014) shouldBe true
    }

    "return true for Start: None and End: 2013-4-6, taxyear 2013" in {
      NpsSchemeMembership(None, Some(NpsDate(2013,4,6))).existsInTaxYear(2013) shouldBe true
    }

    "return false for Start: None and End: 2013-4-5, taxyear 2013" in {
      NpsSchemeMembership(None, Some(NpsDate(2013,4,5))).existsInTaxYear(2013) shouldBe false
    }

    "return true for Start: 2014-4-5 and End: None, taxyear 2013" in {
      NpsSchemeMembership(Some(NpsDate(2014,4,5)), None).existsInTaxYear(2013) shouldBe true
    }

    "return false for Start: 2014-4-6 and End: None, taxyear 2013" in {
      NpsSchemeMembership(Some(NpsDate(2014,4,6)), None).existsInTaxYear(2013) shouldBe false
    }

    "return true for Start: 2013-4-5 and End: 2013-4-6, taxyear 2013" in {
      NpsSchemeMembership(Some(NpsDate(2013,4,5)), Some(NpsDate(2013,4,6))).existsInTaxYear(2013) shouldBe true
    }

    "return false for Start: 2013-4-5 and End: 2013-4-5, taxyear 2013" in {
      NpsSchemeMembership(Some(NpsDate(2013,4,5)), Some(NpsDate(2013,4,5))).existsInTaxYear(2013) shouldBe false
    }

    "return true for Start: 2013-5-5 and End: 2013-6-5, taxyear 2013" in {
      NpsSchemeMembership(Some(NpsDate(2013,5,5)), Some(NpsDate(2013,6,5))).existsInTaxYear(2013) shouldBe true
    }

    "return true for Start: 2013-5-5 and End: 2013-6-5, taxyear 2014" in {
      NpsSchemeMembership(Some(NpsDate(2013,5,5)), Some(NpsDate(2013,6,5))).existsInTaxYear(2013) shouldBe true
    }
  }
}
