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

case class NpsAmountA2016(basicPension: BigDecimal,
                          pre97ApOption: Option[BigDecimal],
                          post97ApOption: Option[BigDecimal],
                          post02ApOption: Option[BigDecimal],
                          pre88GmpOption: Option[BigDecimal],
                          post88GmpOption: Option[BigDecimal],
                          pre88CodOption: Option[BigDecimal],
                          post88CodOption: Option[BigDecimal],
                          grb: BigDecimal) {

  val pre88Gmp: BigDecimal = pre88GmpOption.getOrElse(0)
  val post88Gmp: BigDecimal = post88GmpOption.getOrElse(0)
  val pre88Cod: BigDecimal = pre88CodOption.getOrElse(0)
  val post88Cod: BigDecimal = post88CodOption.getOrElse(0)

  val pre97AP: BigDecimal = pre97ApOption.getOrElse(0)
  val post97AP: BigDecimal = post97ApOption.getOrElse(0)
  val post02AP: BigDecimal = post02ApOption.getOrElse(0)

  val totalAP: BigDecimal = (pre97AP - (pre88Gmp + post88Gmp + pre88Cod + post88Cod)).max(0) + post97AP + post02AP + grb
  val total: BigDecimal = totalAP + basicPension
}



object NpsAmountA2016 {
  implicit val formats: Format[NpsAmountA2016] = (
      (__ \ "ltb_cat_a_cash_value").format[BigDecimal] and
      (__ \ "ltb_pre97_ap_cash_value").format[Option[BigDecimal]] and
      (__ \ "ltb_post97_ap_cash_value").format[Option[BigDecimal]] and
      (__ \ "ltb_post02_ap_cash_value").format[Option[BigDecimal]] and
      (__ \ "pre88_gmp").format[Option[BigDecimal]] and
      (__ \ "ltb_pst88_gmp_cash_value").format[Option[BigDecimal]] and
      (__ \ "ltb_pre88_cod_cash_value").format[Option[BigDecimal]] and
      (__ \ "ltb_post88_cod_cash_value").format[Option[BigDecimal]] and
      (__ \ "grb_cash").format[BigDecimal]
    )(NpsAmountA2016.apply, unlift(NpsAmountA2016.unapply))
}
