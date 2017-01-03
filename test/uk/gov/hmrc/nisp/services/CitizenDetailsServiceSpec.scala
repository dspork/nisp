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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.CitizenDetailsConnector
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class CitizenDetailsServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with ScalaFutures with OneAppPerSuite {
  val nino = TestAccountBuilder.regularNino
  val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  implicit val hc = HeaderCarrier()
  val citizenDetailsService = new CitizenDetailsService {
    override val citizenDetailsConnector: CitizenDetailsConnector = mockCitizenDetailsConnector
  }

  "CitizenDetailsService" should {
    "return ManualCorrespondenceIndicator status is false when Response is 200" in {
      when(mockCitizenDetailsConnector.connectToGetPersonDetails(Matchers.any())(Matchers.any())).thenReturn(
        Future.successful(OK)
      )
      val resultF = citizenDetailsService.retrieveMCIStatus(nino)(hc)
      await(resultF) shouldBe false
    }
    "return ManualCorrespondenceIndicator status is true when Response is 423" in {
      when(mockCitizenDetailsConnector.connectToGetPersonDetails(Matchers.any())(Matchers.any())) thenReturn
        Future.successful(LOCKED)

      val resultF = citizenDetailsService.retrieveMCIStatus(nino)(hc)
      await(resultF) shouldBe true
    }
  }
}
