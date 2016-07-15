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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.services.StatePensionService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.microservice.controller.BaseController

trait StatePensionController extends BaseController {

  val statePensionService: StatePensionService

  def get(nino: Nino): Action[AnyContent] = Action.async {
    implicit request => {
      statePensionService.getStatement(nino).map {
        case Left(exclusion) => Ok(Json.toJson(exclusion))
        case Right(statePension) => Ok(Json.toJson(statePension))
      } recover {
        case e: NotFoundException => NotFound
        case e: GatewayTimeoutException => GatewayTimeout
        case e: BadGatewayException => BadGateway("Unable to connect to NPS")
        case e: BadRequestException => BadRequest("NPS Returned Bad Request. Is this customer over state pension age?")
        case e: Upstream4xxResponse => BadGateway("NPS returned 4XX")
        case e: Upstream5xxResponse => BadGateway("NPS returned 5XX")
        case _ => InternalServerError
      }
    }
  }
}

object StatePensionController extends StatePensionController {
  override val statePensionService: StatePensionService = StatePensionService
}
