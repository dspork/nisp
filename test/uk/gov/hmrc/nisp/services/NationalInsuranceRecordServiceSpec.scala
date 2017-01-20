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

package uk.gov.hmrc.nisp.services

import javassist.NotFoundException

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.domain.TaxYear
import uk.gov.hmrc.nisp.helpers.{StubNationalInsuranceRecordService, TestAccountBuilder}
import uk.gov.hmrc.nisp.models
import uk.gov.hmrc.nisp.models.{NationalInsuranceRecord, NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear}
import uk.gov.hmrc.nisp.models.enums.Exclusion
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, NotFoundException, Upstream4xxResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class NationalInsuranceRecordServiceSpec extends UnitSpec with OneAppPerSuite {
  // scalastyle:off magic.number
  
  val nino = TestAccountBuilder.regularNino
  val exclusionNino = TestAccountBuilder.excludedNino
  val taxYear2010 = TaxYear("2010-11")
  val taxYear2014 = TaxYear("2014-15")

  val taxYear2010TestData: NationalInsuranceRecordTaxYear = NationalInsuranceRecordTaxYear(
    taxYear = "2010-11",
    qualifying = true,
    classOneContributions = 0,
    classTwoCredits = 52,
    classThreeCredits = 0,
    otherCredits = 0,
    classThreePayable = 0,
    classThreePayableBy = None,
    classThreePayableByPenalty = None,
    payable = false,
    underInvestigation = false
  )

  val exclusionTestData: NationalInsuranceRecordExclusion = NationalInsuranceRecordExclusion(
    exclusionReasons = List(Exclusion.MarriedWomenReducedRateElection)
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getTaxYear" should {
    "return valid response for regular nino" in {
      val nationalInsuranceRecord: Either[NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear] =
        StubNationalInsuranceRecordService.getTaxYear(nino, taxYear2010)
      nationalInsuranceRecord.isRight shouldBe true
      nationalInsuranceRecord shouldBe Right(taxYear2010TestData)
    }

    "return a Exclusion Message and details for the excluded Nino" in {
      val nationalInsuranceRecordTaxYear: Either[NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear] =
        StubNationalInsuranceRecordService.getTaxYear(exclusionNino, taxYear2010)
      nationalInsuranceRecordTaxYear.isRight shouldBe false
      nationalInsuranceRecordTaxYear shouldBe Left(exclusionTestData)
    }
  }
  
  "getSummary" when {

    "there is a regular nino" should {

      val nationalInsuranceRecordResponse: Either[NationalInsuranceRecordExclusion, NationalInsuranceRecord] =
                StubNationalInsuranceRecordService.getSummary(nino)

      "return a Right(NationalInsuranceRecord)" in {
        nationalInsuranceRecordResponse.isRight shouldBe true
        nationalInsuranceRecordResponse.right.get shouldBe a [NationalInsuranceRecord]
      }

      val record = nationalInsuranceRecordResponse.right.get

      "return 27 qualifying years" in {
        record.qualifyingYears shouldBe 27
      }

      "return 2 qualifyingYearsPriorTo1975" in {
        record.qualifyingYearsPriorTo1975 shouldBe 2
      }

      "return 13 numberOfGaps" in {
        record.numberOfGaps shouldBe 13
      }

      "return 0 numberOfGapsPayable" in {
        record.numberOfGapsPayable shouldBe 0
      }

      "return 01/01/1973 dateOfEntry" in {
        record.dateOfEntry shouldBe new LocalDate(1973, 10, 1)
      }

      "return false homeResponsibilitiesProtection" in {
        record.homeResponsibilitiesProtection shouldBe false
      }

      "return 05/04/2014 earningsIncludedUpTo" in {
        record.earningsIncludedUpTo shouldBe new LocalDate(2014, 4, 5)
      }

      "return 38 tax years" in {
        record.taxYears.length shouldBe 39
      }

      "the first tax year" should {

        "be the 2013-14 tax year" in {
          record.taxYears.head.taxYear shouldBe "2013-14"
        }

        "be qualifying" in {
          record.taxYears.head.qualifying shouldBe true
        }

        "have 0 class one contributions" in {
          record.taxYears.head.classOneContributions shouldBe 0
        }

        "have 52 class two credits" in {
          record.taxYears.head.classTwoCredits shouldBe 52
        }

        "have 0 class three credits" in {
          record.taxYears.head.classThreeCredits shouldBe 0
        }

        "have 0 other credits" in {
          record.taxYears.head.otherCredits shouldBe 0
        }

        "have 0 class three payable" in {
          record.taxYears.head.classThreePayable shouldBe 0
        }

        "have None class three payable by" in {
          record.taxYears.head.classThreePayableBy shouldBe None
        }

        "have None class three payable by penalty" in {
          record.taxYears.head.classThreePayableByPenalty shouldBe None
        }

        "have payable is equal to false" in {
          record.taxYears.head.payable shouldBe false
        }

        "have under investigation is equal to false" in {
          record.taxYears.head.underInvestigation shouldBe false
        }

      }

      "the last tax year" should {

        "be the 1975-76 tax year" in {
          record.taxYears.last.taxYear shouldBe "1975-76"
        }

        "be qualifying" in {
          record.taxYears.last.qualifying shouldBe true
        }

        "have 109.80 class one contributions" in {
          record.taxYears.last.classOneContributions shouldBe 109.08
        }

        "have 0 class two credits" in {
          record.taxYears.last.classTwoCredits shouldBe 0
        }

        "have 0 class three credits" in {
          record.taxYears.last.classThreeCredits shouldBe 0
        }

        "have 0 other credits" in {
          record.taxYears.last.otherCredits shouldBe 0
        }

        "have 0 class three payable" in {
          record.taxYears.last.classThreePayable shouldBe 0
        }

        "have None class three payable by" in {
          record.taxYears.last.classThreePayableBy shouldBe None
        }

        "have None class three payable by penalty" in {
          record.taxYears.last.classThreePayableByPenalty shouldBe None
        }

        "have payable is equal to false" in {
          record.taxYears.last.payable shouldBe false
        }

        "have under investigation is equal to false" in {
          record.taxYears.last.underInvestigation shouldBe false
        }

      }

    }

    "return a Exclusion Message and details for the excluded Nino" in {
      val nationalInsuranceRecord: Either[NationalInsuranceRecordExclusion, NationalInsuranceRecord] =
        StubNationalInsuranceRecordService.getSummary(exclusionNino)
      nationalInsuranceRecord.isRight shouldBe false
      nationalInsuranceRecord shouldBe Left(exclusionTestData)
    }
  }


  "calc pre75 years" should {
    "return 3 when the number of conts in 157 and the date of entry is 04/10/1972 and their date of birth is 04/10/1956" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(1972, 10, 4), NpsDate(1956, 10, 4)) shouldBe Some(3)
    }
    "return 8 when the number of conts in 408 and the date of entry is 08/01/1968 and their date of birth is 08/01/1952" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(408, NpsDate(1968, 1, 8), NpsDate(1952, 1, 8)) shouldBe Some(8)
    }
    "return 2 when the number of conts in 157 and the date of entry is 06/04/1973 and their date of birth is 04/10/1956" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(1973, 4, 6), NpsDate(1956, 10, 4)) shouldBe Some(2)
    }
    "return 1 when the number of conts in 157 and the date of entry is 06/04/1973 and their date of birth is 06/04/1958" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(1973, 4, 6), NpsDate(1958, 4, 6)) shouldBe Some(1)
    }
    "return 3 when the number of conts in 157 and the date of entry is 06/04/1973 and their date of birth is 24/05/1996" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(1973, 4, 6), NpsDate(1996, 5, 24)) shouldBe None
    }
    "return 3 when the number of conts in 157 and the date of entry is 06/04/1976 and their date of birth is 06/04/1960" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(1976, 4, 6), NpsDate(1960, 4, 6)) shouldBe None
    }
    "return 3 when the number of conts in 157 and the date of entry is 06/04/2005 and their date of birth is 06/04/1958" in {
      StubNationalInsuranceRecordService.calcPre75QualifyingYears(157, NpsDate(2005, 4, 6), NpsDate(1958, 4, 6)) shouldBe None
    }
  }
  
}
