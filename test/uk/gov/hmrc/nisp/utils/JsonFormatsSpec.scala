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

import play.api.libs.json.{JsNull, JsNumber, JsString, JsSuccess}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.nisp.utils.JsonFormats._

class JsonFormatsSpec extends UnitSpec {

  private val BigDecimalTestData: BigDecimal = 22.12
  private val IntTestData: Int = 42

  "write Option[BigDecimal]" should {
    "return a number when present" in {
      OptionBigDecimalFormat.writes(Option[BigDecimal](BigDecimalTestData)) shouldBe JsNumber(BigDecimalTestData)
    }

    "return null when no number is present" in {
      OptionBigDecimalFormat.writes(None) shouldBe JsNull
    }
  }

  "read Option[BigDecimal]" should {
    "return a number when present" in {
      OptionBigDecimalFormat.reads(JsNumber(BigDecimalTestData)) shouldBe JsSuccess(Some(BigDecimalTestData))
    }

    "return null when no number is present" in {
      OptionBigDecimalFormat.reads(JsNull) shouldBe JsSuccess(None)
    }

    "return null when a string is present" in {
      OptionBigDecimalFormat.reads(JsString("null")) shouldBe JsSuccess(None)
    }
  }

  "write Option[Int]" should {
    "return a number when present" in {
      OptionIntFormat.writes(Option[Int](IntTestData)) shouldBe JsNumber(IntTestData)
    }

    "return null when no number is present" in {
      OptionIntFormat.writes(None) shouldBe JsNull
    }
  }

  "read Option[Int]" should {
    "return a number when present" in {
      OptionIntFormat.reads(JsNumber(IntTestData)) shouldBe JsSuccess(Some(IntTestData))
    }

    "return null when no number is present" in {
      OptionIntFormat.reads(JsNull) shouldBe JsSuccess(None)
    }

    "return null when a string is present" in {
      OptionIntFormat.reads(JsString("null")) shouldBe JsSuccess(None)
    }
  }

}
