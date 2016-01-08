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

import uk.gov.hmrc.play.test.UnitSpec

class NpsAmountA2016Spec extends UnitSpec {
  "Total AP Calculation" should {
    "return £4321 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), None, None, None, None, 4000).totalAP shouldBe 4321
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, pre88 gmp 1" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), Some(1), None, None, None, 4000).totalAP shouldBe 4320
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, pre88 gmp 2" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), Some(2), None, None, None, 4000).totalAP shouldBe 4320
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, post88 gmp 1" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), None, Some(1), None, None, 4000).totalAP shouldBe 4320
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, pre88 cod 1" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), None, None, Some(1), None, 4000).totalAP shouldBe 4320
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, post88 cod 1" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), None, None, None, Some(1), 4000).totalAP shouldBe 4320
    }

    "return £4320 for pre97 AP 1, post97 AP 20, post02 AP 300, grb 4000, post88 cod 1, pre88 cod 1" in {
      NpsAmountA2016(115.95, Some(1), Some(20), Some(300), None, None, Some(1), Some(1), 4000).totalAP shouldBe 4320
    }

    "return £4319 for pre97 AP 1, post97 AP 19, post02 AP 300, grb 4000, post88 cod 1, pre88 cod 1" in {
      NpsAmountA2016(115.95, Some(1), Some(19), Some(300), None, None, Some(1), Some(1), 4000).totalAP shouldBe 4319
    }

    "return £4319 for pre97 AP 1, post97 AP 19, post02 AP 300, grb 4000, post88 cod 2, pre88 cod 1" in {
      NpsAmountA2016(115.95, Some(1), Some(19), Some(300), None, None, Some(2), Some(1), 4000).totalAP shouldBe 4319
    }
  }
}
