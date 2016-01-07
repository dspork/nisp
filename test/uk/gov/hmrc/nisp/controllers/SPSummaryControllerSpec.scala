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

package uk.gov.hmrc.nisp.controllers

import org.scalatestplus.play.OneAppPerSuite
import play.api.http._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockSPResponseService}
import uk.gov.hmrc.nisp.services.SPResponseService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SPSummaryControllerSpec extends UnitSpec with OneAppPerSuite {
  val nino = TestAccountBuilder.regularNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino
  
  val testSPSummaryController = new SPSummaryController {
    override def spService: SPResponseService = MockSPResponseService
  }

  "SPSummaryController" should {
    "return 200 with correct NINO" in {
      val result = testSPSummaryController.getSPSummary(nino)(FakeRequest())
      status(result) should be (Status.OK)
    }

    "return JSON" in {
      val result = testSPSummaryController.getSPSummary(nino)(FakeRequest())
      contentType(result) shouldBe Some("application/json")
      charset(result) shouldBe Some("utf-8")
    }

    "return JSON containing nino" in {
      val result = Future.successful(testSPSummaryController.getSPSummary(nino)(FakeRequest()))
      val rawJson = Json.parse(contentAsString(result))
      (rawJson \ "spSummary" \ "nino").as[String] shouldBe nino.value
    }

    "return 404 if missing user" in {
      val result = testSPSummaryController.getSPSummary(nonExistentNino)(FakeRequest())
      status(result) should be (Status.NOT_FOUND)
    }
  }

}
