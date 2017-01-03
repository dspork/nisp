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

import uk.gov.hmrc.nisp.utils.NISPConstants
import uk.gov.hmrc.play.test.UnitSpec

class NpsSummaryModelSpec extends UnitSpec {

  private def summaryModel(countryCode: Int) = NpsSummaryModel("", None, countryCode, NpsDate(), None, NpsDate(), 30,
    2017, None, None, None, None, None, NpsDate(), None, 0, "F", NpsStatePensionAmount(None, None, None, None,
    NpsAmountA2016(0, None, None, None, None, None, None, None, 0), NpsAmountB2016(None, None)),
    NpsPensionForecast(0, 0, 0, 0))

  "isAbroad" should {
    "return false if the country code is not specified" in {
       summaryModel(NISPConstants.countryNotSpecified).isAbroad shouldBe false
    }

    "return true if the country code is JERSEY" in {
       summaryModel(4).isAbroad shouldBe true
    }

    "return true if the country code is MOZAMBIQUE" in {
       summaryModel(10).isAbroad shouldBe true
    }

    "return false if the country code is GREAT BRITAIN" in {
       summaryModel(NISPConstants.countryGB).isAbroad shouldBe false
    }

    "return false if the country code is ENGLAND" in {
       summaryModel(NISPConstants.countryEngland).isAbroad shouldBe false
    }

    "return false if the country code is SCOTLAND" in {
       summaryModel(NISPConstants.countryScotland).isAbroad shouldBe false
    }

    "return false if the country code is WALES" in {
       summaryModel(NISPConstants.countryWales).isAbroad shouldBe false
    }

    "return false if the country code is NORTHERN IRELAND" in {
       summaryModel(NISPConstants.countryNI).isAbroad shouldBe false
    }

    "return false if the country code is ISLE OF MAN" in {
       summaryModel(NISPConstants.countryIsleOfMan).isAbroad shouldBe false
    }
  }
}