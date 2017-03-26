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

package uk.gov.hmrc.nisp.controllers

import org.joda.time.LocalDate
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nisp.domain.TaxYear
import uk.gov.hmrc.nisp.helpers.{StubNationalInsuranceRecordService, TestAccountBuilder}
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.services.NationalInsuranceRecordService
import uk.gov.hmrc.play.test.UnitSpec

class NationalInsuranceRecordControllerSpec extends UnitSpec with OneAppPerSuite {

  val nino = TestAccountBuilder.regularNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino
  val taxYear2010 = TaxYear("2010-11")
  val taxYear2015 = TaxYear("2015-16")

  def testNationalInsuranceRecordController(newAPIConfig: Boolean = false): NationalInsuranceRecordController = new NationalInsuranceRecordController {
    override val nationalInsuranceRecordService: NationalInsuranceRecordService = StubNationalInsuranceRecordService
  }

  "getSummary" should {
    "return 200 status for existing NINO" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      status(result) shouldBe Status.OK
    }

    "return 404 if incorrect NINO" in {
      val result = testNationalInsuranceRecordController().getSummary(nonExistentNino)(FakeRequest())
      status(result) shouldBe Status.NOT_FOUND
    }

    "return JSON for existing NINO" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      contentType(result) shouldBe Some("application/json")
    }

    "return JSON with tax years" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      ((rawJson \ "taxYears").as[List[JsValue]].head \ "taxYear").as[String] shouldBe "2013-14"
    }

    "return JSON with qualifyingYearsPriorTo1975" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "qualifyingYearsPriorTo1975").as[Int] shouldBe 2
    }

    "return JSON with qualifyingYears" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "qualifyingYears").as[Int] shouldBe 27
    }

    "return JSON with numberOfGaps" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "numberOfGaps").as[Int] shouldBe 13
    }

    "return JSON with numberOfGapsPayable" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "numberOfGapsPayable").as[Int] shouldBe 0
    }

    "return JSON with dateOfEntry" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "dateOfEntry").as[LocalDate] shouldBe new LocalDate(1973, 10, 1)
    }

    "return JSON with homeResponsibilitiesProtection" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "homeResponsibilitiesProtection").as[Boolean] shouldBe false
    }

    "return JSON with earningsIncludedUpTo" in {
      val result = testNationalInsuranceRecordController().getSummary(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "earningsIncludedUpTo").as[LocalDate] shouldBe new LocalDate(2014, 4, 5)
    }
  }

  "getTaxYear" should {
    "return 200 status for existing NINO" in {
      val result = testNationalInsuranceRecordController().getTaxYear(nino, taxYear2010)(FakeRequest())
      status(result) shouldBe Status.OK
    }

    "return 404 if incorrect NINO" in {
      val result = testNationalInsuranceRecordController().getTaxYear(nonExistentNino, taxYear2010)(FakeRequest())
      status(result) shouldBe Status.NOT_FOUND
    }

    "return 404 if existing NINO and missing taxYear" in {
      val result = testNationalInsuranceRecordController().getTaxYear(nino, taxYear2015)(FakeRequest())
      status(result) shouldBe Status.NOT_FOUND
    }

    "return JSON for existing NINO" in {
      val result = testNationalInsuranceRecordController().getTaxYear(nino, taxYear2010)(FakeRequest())
      contentType(result) shouldBe Some("application/json")
    }

    "return JSON with taxYear, underInvestigation flag and classThreePayableBy date" in {
      val result = testNationalInsuranceRecordController().getTaxYear(nino,taxYear2010)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "taxYear").as[String] shouldBe "2010-11"
      (rawJson \ "underInvestigation").as[Boolean] shouldBe false
      (rawJson \ "classThreePayableBy").asOpt[LocalDate] shouldBe None
    }

  }

}
