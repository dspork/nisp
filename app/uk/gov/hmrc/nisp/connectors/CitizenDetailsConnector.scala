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

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.config.wiring.WSHttp
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}

import scala.concurrent.Future
import play.api.http.Status._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.util.{Success, Try, Failure}

object CitizenDetailsConnector  extends CitizenDetailsConnector with ServicesConfig {
  override val serviceUrl = baseUrl("citizen-details")
  override val http: HttpGet = WSHttp
  override val metrics: Metrics = Metrics
}

trait CitizenDetailsConnector {
  val serviceUrl: String
  val http: HttpGet
  val metrics: Metrics

  private def url(nino: Nino) = s"$serviceUrl/citizen-details/$nino/designatory-details/"

  def connectToGetPersonDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[Int] = {
    val timerContext = metrics.startCitizenDetailsTimer()
    http.GET[HttpResponse](url(nino)) map {
      personResponse =>
        timerContext.stop()
        Success(personResponse.status)
    } recover {
      case ex: Upstream4xxResponse if ex.upstreamResponseCode == LOCKED => timerContext.stop(); Success(ex.upstreamResponseCode)
      case ex: Throwable => timerContext.stop(); Failure(ex)
    } flatMap (handleResult(url(nino), _))
  }

  private def handleResult[A](url: String, tryResult: Try[A]): Future[A] = {
    tryResult match {
      case Failure(ex) =>
        Future.failed(ex)
      case Success(value) =>
        Future.successful(value)
    }
  }

}
