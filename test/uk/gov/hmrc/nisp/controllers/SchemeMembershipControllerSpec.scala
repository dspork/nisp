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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, StubSchemeMembershipService}
import uk.gov.hmrc.nisp.models.SchemeMembership
import uk.gov.hmrc.nisp.services.SchemeMembershipService
import uk.gov.hmrc.play.test.UnitSpec

class SchemeMembershipControllerSpec  extends UnitSpec with OneAppPerSuite {

  val nino = TestAccountBuilder.regularNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino

  def testSchemeSummaryController: SchemeMembershipController = new SchemeMembershipController {
    override def schemeService: SchemeMembershipService = StubSchemeMembershipService
  }

  "return 200 for existing NINO" in {
    val result = testSchemeSummaryController.getSchemeSummary(nino)(FakeRequest())
    status(result) should be (Status.OK)
  }

  "return 404 if missing user" in {
    val result = testSchemeSummaryController.getSchemeSummary(nonExistentNino)(FakeRequest())
    status(result) should be (Status.NOT_FOUND)
  }

  "return JSON list of SchemeMembership details for the contracted out user" in {
    val result = testSchemeSummaryController.getSchemeSummary(nino)(FakeRequest())
    val rawJson = Json.parse(contentAsString(result))
    Json.fromJson[List[SchemeMembership]](rawJson).get shouldBe
      List(
        SchemeMembership(new LocalDate(1999,4,6), new LocalDate(2016, 4, 5)),
        SchemeMembership(new LocalDate(1978,4,6), new LocalDate(1997,6,30))
      )
  }

}
