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

package uk.gov.hmrc.nisp.utils

import play.api.libs.json._

object JsonFormats {

  implicit object OptionBigDecimalFormat extends Format[Option[BigDecimal]] {
    override def writes(o: Option[BigDecimal]): JsValue = o match {
      case v: Some[BigDecimal]  => JsNumber(v.get)
      case None                 => JsNull
    }

    override def reads(json: JsValue): JsResult[Option[BigDecimal]] = {
      JsSuccess(json.asOpt[BigDecimal])
    }
  }

  implicit object OptionIntFormat extends Format[Option[Int]] {
    override def writes(o: Option[Int]): JsValue = o match {
      case v: Some[Int]         => JsNumber(v.get)
      case None                 => JsNull
    }

    override def reads(json: JsValue): JsResult[Option[Int]] = {
      JsSuccess(json.asOpt[Int])
    }
  }

}
