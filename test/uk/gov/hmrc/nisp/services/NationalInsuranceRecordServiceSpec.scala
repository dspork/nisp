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
import uk.gov.hmrc.nisp.models.{NationalInsuranceRecord, NationalInsuranceRecordExclusion, NationalInsuranceRecordTaxYear, TaxYearSummary}
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
    classThreePayable = None,
    classThreePayableBy = None,
    classThreePayableByPenalty = None,
    payable = false,
    underInvestigation = false
  )

  val niSummary: NationalInsuranceRecord = NationalInsuranceRecord(
      qualifyingYears =  27,
      qualifyingYearsPriorTo1975 =  2,
      numberOfGaps =  13,
      numberOfGapsPayable =  0,
      dateOfEntry =   new LocalDate(1973,10,1),
      homeResponsibilitiesProtection =  false,
      earningsIncludedUpTo = new LocalDate(2014, 4, 5),
      List(
        TaxYearSummary("1975-76",true),TaxYearSummary("1976-77",true),TaxYearSummary("1977-78",true),
        TaxYearSummary("1978-79",true),TaxYearSummary("1979-80",true),TaxYearSummary("1980-81",false),
        TaxYearSummary("1981-82",true),TaxYearSummary("1982-83",true), TaxYearSummary("1983-84",true),
        TaxYearSummary("1984-85",true),TaxYearSummary("1985-86",true),TaxYearSummary("1986-87",true),
        TaxYearSummary("1987-88",true),TaxYearSummary("1988-89",true),TaxYearSummary("1989-90",true),
        TaxYearSummary("1990-91",true),TaxYearSummary("1991-92",true),TaxYearSummary("1992-93",true),
        TaxYearSummary("1993-94",false),TaxYearSummary("1994-95",false),TaxYearSummary("1995-96",false),
        TaxYearSummary("1996-97",false),TaxYearSummary("1997-98",false),TaxYearSummary("1998-99",false),
        TaxYearSummary("1999-00",false),TaxYearSummary("2000-01",false),TaxYearSummary("2001-02",false),
        TaxYearSummary("2002-03",false),TaxYearSummary("2003-04",false),TaxYearSummary("2004-05",false),
        TaxYearSummary("2005-06",true),TaxYearSummary("2006-07",true),TaxYearSummary("2007-08",true),
        TaxYearSummary("2008-09",true),TaxYearSummary("2009-10",true),TaxYearSummary("2010-11",true),
        TaxYearSummary("2011-12",true),TaxYearSummary("2012-13",true),TaxYearSummary("2013-14",true)
      )
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
  
  "getSummary" should {

    "return valid response for regular nino" in {
      val nationalInsuranceRecord: Either[NationalInsuranceRecordExclusion, NationalInsuranceRecord] =
        StubNationalInsuranceRecordService.getSummary(nino)
      nationalInsuranceRecord.isRight shouldBe true
      nationalInsuranceRecord shouldBe Right(niSummary)
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
