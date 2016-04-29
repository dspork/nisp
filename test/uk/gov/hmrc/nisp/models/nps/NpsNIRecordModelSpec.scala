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

class NpsNIRecordModelSpec extends UnitSpec {

  def taxYear(year: Int, qualifying: Boolean, payable: Boolean): NpsNITaxYear = NpsNITaxYear(year, if (qualifying) 1 else 0, 0, if (payable) 1 else 0, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)

  "purge" should {
    "return an nirecord with no tax years after 2014 when the FRY 2014" in {
      val niRecord = NpsNIRecordModel("", 5, 2, 0, 2, pre75ContributionCount = 0, NpsDate(2010, 4, 6), List(
        taxYear(2010, true, false),
        taxYear(2011, true, false),
        taxYear(2012, true, false),
        taxYear(2013, true, false),
        taxYear(2014, true, false),
        taxYear(2015, false, true),
        taxYear(2016, false, true)
      ))

      val purged = niRecord.purge(fry = 2014)

      purged.nino shouldBe ""
      purged.numberOfQualifyingYears shouldBe 5
      purged.nonQualifyingYears shouldBe 0
      purged.yearsToFry shouldBe 0
      purged.nonQualifyingYearsPayable shouldBe 0
      purged.pre75ContributionCount shouldBe 0
      purged.dateOfEntry shouldBe NpsDate(2010, 4, 6)
      purged.niTaxYears shouldBe List(
        taxYear(2010, true, false),
        taxYear(2011, true, false),
        taxYear(2012, true, false),
        taxYear(2013, true, false),
        taxYear(2014, true, false)
      )
    }
  }

  "return an nirecord with no tax years after 2015 when the FRY 2015" in {
    val niRecord = NpsNIRecordModel("", 3, 4, 0, 3, pre75ContributionCount = 0, NpsDate(2010, 4, 6), List(
      taxYear(2010, true, false),
      taxYear(2011, false, false),
      taxYear(2012, false, true),
      taxYear(2013, true, false),
      taxYear(2014, true, false),
      taxYear(2015, false, true),
      taxYear(2016, false, true)
    ))

    val purged = niRecord.purge(fry = 2015)

    purged.nino shouldBe ""
    purged.numberOfQualifyingYears shouldBe 3
    purged.nonQualifyingYears shouldBe 3
    purged.yearsToFry shouldBe 0
    purged.nonQualifyingYearsPayable shouldBe 2
    purged.pre75ContributionCount shouldBe 0
    purged.dateOfEntry shouldBe NpsDate(2010, 4, 6)
    purged.niTaxYears shouldBe List(
      taxYear(2010, true, false),
      taxYear(2011, false, false),
      taxYear(2012, false, true),
      taxYear(2013, true, false),
      taxYear(2014, true, false),
      taxYear(2015, false, true)
    )
  }
}