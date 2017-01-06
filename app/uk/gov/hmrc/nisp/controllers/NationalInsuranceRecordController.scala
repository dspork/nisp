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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.domain.TaxYear
import uk.gov.hmrc.nisp.services.NationalInsuranceRecordService
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._


trait NationalInsuranceRecordController extends BaseController with ErrorHandling {
  val nationalInsuranceRecordService: NationalInsuranceRecordService
  val method = "NationalInsuranceRecord"

  def getTaxYear(nino: Nino, taxYear: TaxYear): Action[AnyContent] = Action.async {
    implicit request => errorWrapper(nino, {
      nationalInsuranceRecordService.getTaxYear(nino, taxYear).map {
        case Left(exclusion) => Ok(Json.toJson(exclusion))
        case Right(nationalInsuranceRecordTaxYear) => Ok(Json.toJson(nationalInsuranceRecordTaxYear))
      }
    })
  }

  def getSummary(nino: Nino): Action[AnyContent] = Action.async {
    implicit request => errorWrapper(nino, {
      nationalInsuranceRecordService.getSummary(nino).map {
        case Left(exclusion) => Ok(Json.toJson(exclusion))
        case Right(nationalInsuranceRecord) => Ok(Json.toJson(nationalInsuranceRecord))
      }
    })
  }

}

object NationalInsuranceRecordController extends NationalInsuranceRecordController {
  override val nationalInsuranceRecordService: NationalInsuranceRecordService = NationalInsuranceRecordService
}
