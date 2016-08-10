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

package uk.gov.hmrc.nisp.connectors

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeRequest
import uk.gov.hmrc.nisp.helpers.{StubMetrics, TestAccountBuilder}
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CitizenDetailsConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with ScalaFutures with OneAppPerSuite {
  val nino = TestAccountBuilder.regularNino
  lazy val fakeRequest = FakeRequest()
  implicit val hc = HeaderCarrier()
  val citizenDetailsConnector = new CitizenDetailsConnector {
    override val serviceUrl: String = "/"
    override val http: HttpGet = mock[HttpGet]
    override val metrics: Metrics = StubMetrics
  }

  "CitizenDetailsConnector" should {
    "return OK status when successful" in {
      when(citizenDetailsConnector.http.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())) thenReturn Future.successful(HttpResponse(200))
      val resultF = citizenDetailsConnector.connectToGetPersonDetails(nino)(hc)
      await(resultF) shouldBe 200
    }

    "return 423 status when the Upstream is 423" in {
      when(citizenDetailsConnector.http.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())) thenReturn Future.failed(new Upstream4xxResponse(":(", 423, 423, Map()))
      val resultF = citizenDetailsConnector.connectToGetPersonDetails(nino)(hc)
      await(resultF) shouldBe 423
    }

    "return NotFoundException for invalid nino" in {
      when(citizenDetailsConnector.http.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())) thenReturn Future.failed(new NotFoundException("Not Found"))
      val resultF = citizenDetailsConnector.connectToGetPersonDetails(nino)(hc)
      await(resultF.failed) shouldBe a [NotFoundException]
    }

    "return 500 response code when there is Upstream is 4XX" in {
      when(citizenDetailsConnector.http.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())) thenReturn Future.failed(new InternalServerException("InternalServerError"))
      val resultF = citizenDetailsConnector.connectToGetPersonDetails(nino)(hc)
      await(resultF.failed) shouldBe a [InternalServerException]
    }
  }
}
