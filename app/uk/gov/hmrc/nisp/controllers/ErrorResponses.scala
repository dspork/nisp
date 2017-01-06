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

import uk.gov.hmrc.play.microservice.bootstrap.ErrorResponse

object ErrorResponses {
  val CODE_INVALID_TAXYEAR = "CODE_TAXYEAR_INVALID"
  object ErrorTaxYearInvalid extends ErrorResponse(400, CODE_INVALID_TAXYEAR, Some("The provided TAX YEAR is not valid"))
}
