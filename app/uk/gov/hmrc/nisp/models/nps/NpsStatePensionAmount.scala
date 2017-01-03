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

package uk.gov.hmrc.nisp.models.nps

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class NpsStatePensionAmount(startingAmount2016Option: Option[BigDecimal],
                                 protectedPayment2016Option: Option[BigDecimal],
                                 nspEntitlementOption: Option[BigDecimal],
                                 apAmountOption: Option[BigDecimal],
                                 npsAmountA2016: NpsAmountA2016,
                                 npsAmountB2016: NpsAmountB2016) {
  val protectedPayment2016: BigDecimal = protectedPayment2016Option.getOrElse(0)
  val nspEntitlement: BigDecimal = nspEntitlementOption.getOrElse(0)
  val startingAmount2016: BigDecimal = startingAmount2016Option.getOrElse(0)
  val apAmount: BigDecimal = apAmountOption.getOrElse(0)
}

object NpsStatePensionAmount {
  implicit val formats: Format[NpsStatePensionAmount] = (
      (__ \ "starting_amount").format[Option[BigDecimal]] and
      (__ \ "protected_payment_2016").format[Option[BigDecimal]] and
      (__ \ "nsp_entitlement").format[Option[BigDecimal]] and
      (__ \ "ap_amount").format[Option[BigDecimal]] and
      (__ \ "npsAmnapr16").format[NpsAmountA2016] and
      (__ \ "npsAmnbpr16").format[NpsAmountB2016]
    )(NpsStatePensionAmount.apply, unlift(NpsStatePensionAmount.unapply))
}
