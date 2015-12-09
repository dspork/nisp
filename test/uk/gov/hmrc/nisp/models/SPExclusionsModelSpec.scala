/*
 * Copyright 2015 HM Revenue & Customs
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

package uk.gov.hmrc.nisp.models

import play.api.libs.json._
import uk.gov.hmrc.nisp.models.enums.SPExclusion
import uk.gov.hmrc.play.test.UnitSpec

class SPExclusionsModelSpec extends UnitSpec {

  "serialised into JSON" should {
    "become a list of JsStrings" in {
      Json.toJson(List(SPExclusion.Abroad, SPExclusion.ContractedOut, SPExclusion.CustomerTooOld)) shouldBe
        JsArray(Seq[JsString](JsString("Abroad"), JsString("ContractedOut"), JsString("CustomerTooOld")))
    }

    "become a list of JSString with one exclusion" in {
      Json.toJson(List(SPExclusion.Abroad)) shouldBe
        JsArray(Seq[JsString](JsString("Abroad")))
    }
  }

  "parsed from JSON" should {
    "become a SPExclusionModel with list of Enum values" in {
      val json = Json.parse("{ \"spExclusions\" : [\"Abroad\", \"ContractedOut\", \"CustomerTooOld\"] }")

      Json.fromJson[SPExclusionsModel](json).get shouldBe
        SPExclusionsModel(List(SPExclusion.Abroad, SPExclusion.ContractedOut, SPExclusion.CustomerTooOld))
    }

    "become a SPExclusion model with one enum value in a list" in {
      val json = Json.parse("{ \"spExclusions\" : [\"Abroad\"] }")

      Json.fromJson[SPExclusionsModel](json).get shouldBe
        SPExclusionsModel(List(SPExclusion.Abroad))
    }
  }
}
