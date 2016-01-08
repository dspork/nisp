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

package uk.gov.hmrc.nisp.models.nps

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class NpsAmountB2016(mainComponentOption: Option[BigDecimal],
                          rebateDerivedAmountOption: Option[BigDecimal]) {
  val mainComponent: BigDecimal = mainComponentOption.getOrElse(0)
  val rebateDerivedAmount: BigDecimal = rebateDerivedAmountOption.getOrElse(0)
}

object NpsAmountB2016 {
  implicit val formats: Format[NpsAmountB2016] = (
      (__ \ "main_component").format[Option[BigDecimal]] and
      (__ \ "rebate_derived_amount").format[Option[BigDecimal]]
    )(NpsAmountB2016.apply, unlift(NpsAmountB2016.unapply))
}
