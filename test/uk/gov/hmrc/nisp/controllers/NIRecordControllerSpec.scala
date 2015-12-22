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

package uk.gov.hmrc.nisp.controllers

import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockNIService}
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.nisp.services.NIResponseService
import uk.gov.hmrc.play.test.UnitSpec

class NIRecordControllerSpec extends UnitSpec with OneAppPerSuite {

  val nino = TestAccountBuilder.regularNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino

  def testNIRecordControllerWithMockHttp(newAPIConfig: Boolean = false): NIRecordController = new NIRecordController {
    override def niService: NIResponseService = MockNIService
  }

  "return 200 for existing NINO" in {
    val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
    status(result) should be (Status.OK)
  }

  "return 404 if incorrect NINO" in {
    val result = testNIRecordControllerWithMockHttp().getNIRecord(nonExistentNino)(FakeRequest())
    status(result) should be (Status.NOT_FOUND)
  }

  "return JSON for existing NINO" in {
    val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
    contentType(result) shouldBe Some("application/json")
    charset(result) shouldBe Some("utf-8")
  }

  "return JSON with tax years" in {
    val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
    val rawJson = Json.parse(contentAsString(result))
    ((rawJson \ "niRecord" \ "taxYears").as[List[JsValue]].head \ "taxYear").as[Int] shouldBe 1975
  }

  "customer with 27 Qualifying years" should {
    "returns NIResponse with 27 qualifying years" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "noOfQualifyingYears").as[Int] shouldBe 27
    }
  }

  "customer with 13 non-qualifying years post-75" should {
    "return NIResponse with 13 non-qualifying years" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "noOfNonQualifyingYears").as[Int] shouldBe 13
    }
  }

  "customer with SPA 2022" should {

    "return NIResponse with 3 years to contribute until pension age" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "yearsToContributeUntilPensionAge").as[Int] shouldBe 3
    }

    "return NIResponse with SPA 2017" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "spaYear").as[Int] shouldBe 2017
    }

  }

  "current date is 26 June 2015" should {

    "return earningIncludedUpTo as 5th April 2014" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "earningsIncludedUpTo").as[NpsDate] shouldBe NpsDate(2014, 4, 5)
    }

    "return taxyear as 2014" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "unavailableYear").as[Int] shouldBe 2014
    }

  }

  "customer with dateofentry of 1972" should {
    "return 2 pre 75 qualifying years for 52 pre 75 contributions" in {
      val result = testNIRecordControllerWithMockHttp().getNIRecord(nino)(FakeRequest())
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "niSummary" \ "pre75QualifyingYears").as[Int] shouldBe 2
    }
  }
}
