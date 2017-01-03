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

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder
import uk.gov.hmrc.nisp.services.StatePensionService
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.Matchers

import scala.concurrent.Future

class StatePensionControllerSpec extends UnitSpec with OneAppPerSuite with MockitoSugar{

  val nino = TestAccountBuilder.regularNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino
  lazy val fakeRequest = FakeRequest()
  val mockStatePensionService = mock[StatePensionService]
  val statePensionController = new StatePensionController {
    override val statePensionService: StatePensionService = mockStatePensionService
  }

  "StatePensionService" should {
      "return BadGateway in case of connectivity issues" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new BadGatewayException("Unable to connect to NPS"))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.BAD_GATEWAY
      }

      "return NotFoundException for invalid nino" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new NotFoundException("NotFound"))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.NOT_FOUND
      }

      "return GatewayTimeout when response is delayed than given limit" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new GatewayTimeoutException("GatewayTimeout"))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.GATEWAY_TIMEOUT
      }

      "return BadRequest response when there is bad request" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new BadRequestException("NPS Returns the Bad Request"))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BadGateway response when 4XX response from NPS" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new Upstream4xxResponse("NPS returned 4XX", 403, 403, Map()))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.BAD_GATEWAY
      }

      "return BadGateway response when 5XX response from NPS" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new Upstream4xxResponse("NPS returned 5XX", 503, 503, Map()))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.BAD_GATEWAY
      }

      "return Internal Server Error response in case of any NPS error" in {
        when(mockStatePensionService.getStatement(Matchers.any())(Matchers.any())).thenReturn(
          Future.failed(new InternalServerException("InternalServerError"))
        )
        val result = statePensionController.get(nino).apply(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
  }
}
