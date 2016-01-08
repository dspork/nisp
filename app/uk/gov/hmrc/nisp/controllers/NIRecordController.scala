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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.services.NIResponseService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

object NIRecordController extends NIRecordController {
  override val niService = NIResponseService
}

trait NIRecordController extends BaseController {
  def niService: NIResponseService

  def getNIRecord(nino: Nino) : Action[AnyContent] =
    Action.async { implicit request => {
        Metrics.niRecordCounter.inc()

        niService.getNIResponse(nino).map (niResponse => Ok(Json.toJson(niResponse))) recover {
          case _ =>
            Logger.warn("Something went wrong. Empty NI Response.")
            NotFound
        }
      }
    }
}
