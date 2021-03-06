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

package uk.gov.hmrc.nisp.domain


import org.scalatest.Matchers
import uk.gov.hmrc.play.test.UnitSpec

class TaxYearSpec extends UnitSpec with Matchers {

  val validTaxYears = Seq("2014-15", "2013-14", "2016-17", "2019-20", "2099-00")
  val invalidTaxYears = Seq("2014", "201314", "2016-1X", "A2014-15", "2015-17", "2013-18")

  "isValid" should {
    validTaxYears.foreach {
      taxYear => s"return true for tax year $taxYear" in {
        TaxYear.isValid(taxYear) shouldBe true
      }
    }

    validTaxYears.foreach {
      taxYear => s"return taxYear $taxYear" in {
        TaxYear(taxYear).value shouldBe taxYear
      }
    }

    invalidTaxYears.foreach {
      taxYear => s"return false for tax year $taxYear" in {
        TaxYear.isValid(taxYear) shouldBe false
      }
    }
  }

  "getFormattedTaxYear" should {
    "return valid TaxYear 2010-11 for 2010" in {
      TaxYear.getFormattedTaxYear(2010) shouldBe TaxYear("2010-11")
    }
    "return valid TaxYear 2000-01 for 2000" in {
      TaxYear.getFormattedTaxYear(2000) shouldBe TaxYear("2000-01")
    }
    "return valid TaxYear 1999-00 for 1999" in {
      TaxYear.getFormattedTaxYear(1999) shouldBe TaxYear("1999-00")
    }

  }


  "TaxYear constructor" should {
    validTaxYears.foreach {
      taxYear => s"create a taxYear for a valid argument '$taxYear'" in {
        TaxYear("2014-15").taxYear == taxYear
      }
    }

    invalidTaxYears.foreach {
      taxYear => s"throw an IllegalArgumentException for an invalid argument '$taxYear'" in {
        an[IllegalArgumentException] should be thrownBy TaxYear(taxYear)
      }
    }

    "a valid TaxYear" should {
      "be transformed and startYr should be 2014" in {
        TaxYear("2014-15").startYear shouldBe "2014"
      }
    }

    "a valid TaxYear" should {
      "be transformed and startYear should be 2015" in {
        TaxYear("2015-16").startYear shouldBe "2015"
      }
    }

  }


}
