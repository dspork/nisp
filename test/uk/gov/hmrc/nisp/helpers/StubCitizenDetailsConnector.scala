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

package uk.gov.hmrc.nisp.helpers

import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.connectors.CitizenDetailsConnector
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder._
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, HttpGet}
import scala.concurrent.Future

object StubCitizenDetailsConnector extends CitizenDetailsConnector with MockitoSugar{
  override val serviceUrl: String = ""
  override val http: HttpGet = mock[HttpGet]
  implicit val hc = HeaderCarrier()

  stubSuccess(regularNino)
  private def stubSuccess(nino: Nino): Unit =
    when(http.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(200)))

  override val metrics: Metrics = StubMetrics
}
