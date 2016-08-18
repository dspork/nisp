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

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait ErrorHandling {
  self: BaseController =>

  val method: String

  def errorWrapper(nino: Nino, func: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    func.recover {
      case e: Throwable =>
        Logger.error(s"$method/$nino Unrecoverable Error: ${e.getMessage}", e)
        e match {
          case e: NotFoundException => NotFound
          case e: GatewayTimeoutException => GatewayTimeout
          case e: BadGatewayException => BadGateway("Unable to connect to Upstream")
          case e: BadRequestException => BadRequest("Upstream Bad Request")
          case e: Upstream4xxResponse => BadGateway("Upstream 4XX")
          case e: Upstream5xxResponse => BadGateway("Upstream 5XX")
          case _ => InternalServerError
        }
    }
  }
}
