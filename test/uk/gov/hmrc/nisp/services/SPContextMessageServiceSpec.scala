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

package uk.gov.hmrc.nisp.services

import uk.gov.hmrc.nisp.models.SPAmountModel
import uk.gov.hmrc.nisp.models.enums.SPContextMessage
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.test.UnitSpec

class SPContextMessageServiceSpec extends UnitSpec {

  "Scenario One (Ahmed)" when {
    "the user has an SPAmount >= 151.25 AND QYs >= 30" should {
      "return ScenarioOne" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 30, NpsDate(2015,4,5), 0) shouldBe Some(SPContextMessage.ScenarioOne)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs >= 30" should {
      "not return ScenarioOne" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 30, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioOne)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30" should {
      "not return ScenarioOne" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioOne)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30" should {
      "not return ScenarioOne" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 29, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioOne)
      }
    }
  }

  "Scenario Two (Dorothy)" when {
    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps" should {
      "return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 1) shouldBe Some(SPContextMessage.ScenarioTwo)
      }
    }
    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps earnings included up to 2013/14" should {
      "return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 28, NpsDate(2014,4,5), 1) shouldBe Some(SPContextMessage.ScenarioTwo)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps" should {
      "not return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 29, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioTwo)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs >= 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps" should {
      "not return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 30, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioTwo)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND no fillable NI gaps AND can reach full bSP without filling NI gaps" should {
      "not return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioTwo)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps" should {
      "not return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 28, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioTwo)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps, earnings included up to 2013/14" should {
      "not return ScenarioTwo" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 27, NpsDate(2014,4,5), 1) should not be Some(SPContextMessage.ScenarioTwo)
      }
    }
  }

  "Scenario Three (Derek)" when {
    "the user has an SPAmount >= 151.25 AND QYs < 30 AND no fillable gaps" should {
      "return ScenarioThree" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 0) shouldBe Some(SPContextMessage.ScenarioThree)
      }
    }
  }

  "Scenario Four (Priya)" when {
    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps" should {
      "return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 28, NpsDate(2015,4,5), 1) shouldBe Some(SPContextMessage.ScenarioFour)
      }
    }
    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps earnings included up to 2013/14" should {
      "return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 27, NpsDate(2014,4,5), 1) shouldBe Some(SPContextMessage.ScenarioFour)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps" should {
      "not return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 28, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioFour)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs >= 30 AND fillable NI gaps AND cannot reach full bSP without filling NI gaps" should {
      "not return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 30, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioFour)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND no fillable NI gaps AND cannot reach full bSP without filling NI gaps" should {
      "not return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 28, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioFour)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps" should {
      "not return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioFour)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND fillable NI gaps AND can reach full bSP without filling NI gaps, earnings included up to 2013/14" should {
      "not return ScenarioFour" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 28, NpsDate(2014,4,5), 1) should not be Some(SPContextMessage.ScenarioFour)
      }
    }
  }

  "Scenario Five (Susan)" when {
    "the user has an SPAmount < 151.25 AND QYs >= 30 AND no fillable NI gaps" should {
      "return ScenarioFive" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 35, NpsDate(2015,4,5), 0) shouldBe Some(SPContextMessage.ScenarioFive)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs >= 30 AND 1 fillable NI gaps" should {
      "not return ScenarioFive" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 35, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioFive)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs >= 30 AND no fillable NI gaps" should {
      "not return ScenarioFive" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 35, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioFive)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30 AND no fillable NI gaps" should {
      "not return ScenarioFive" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 29, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioFive)
      }
    }
  }



  "Scenario 6 (Persephone)" when {
    "the user has an SPAmount < 151.25 AND QYs >= 30 AND has fillable NI gaps" should {
      "return ScenarioSix" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 30, NpsDate(2015,4,5), 1) shouldBe Some(SPContextMessage.ScenarioSix)
      }
    }
    "the user has an SPAmount < 151.25 AND QYs >= 30 AND no fillable NI gaps" should {
      "not return ScenarioSix" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 30, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioSix)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs >= 30 AND 1 fillable NI gaps" should {
      "not return ScenarioSix" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 30, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioSix)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30 AND 1 fillable NI gaps" should {
      "not return ScenarioSix" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 29, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioSix)
      }
    }
  }

  "Scenario 7 (Robert)" when {
    "the user has an SPAmount < 151.25 AND QYs < 30 AND no fillable NI gaps" should {
      "return ScenarioSeven" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 24, NpsDate(2015,4,5), 0) shouldBe Some(SPContextMessage.ScenarioSeven)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs < 30 AND 1 fillable NI gap" should {
      "not return ScenarioSeven" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 30, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioSeven)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND no fillable NI gaps" should {
      "not return ScenarioSeven" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 30, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioSeven)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs >= 30 AND no fillable NI gaps" should {
      "not return ScenarioSeven" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 35, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioSeven)
      }
    }
  }

  "Scenario 8 (Tyrone)" when {
    "the user has an SPAmount < 151.25 AND QYs < 30 AND has fillable NI gaps" should {
      "return ScenarioEight" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 24, NpsDate(2015,4,5), 1) shouldBe Some(SPContextMessage.ScenarioEight)
      }
    }
    "the user has an SPAmount < 151.25 AND QYs < 30 AND no fillable NI gaps" should {
      "not return ScenarioEight" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 29, NpsDate(2015,4,5), 0) should not be Some(SPContextMessage.ScenarioEight)
      }
    }

    "the user has an SPAmount >= 151.25 AND QYs < 30 AND 1 fillable NI gaps" should {
      "not return ScenarioEight" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.25), 29, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioEight)
      }
    }

    "the user has an SPAmount < 151.25 AND QYs >= 30 AND 1 fillable NI gaps" should {
      "not return ScenarioEight" in {
        SPContextMessageService.getSPContextMessage(SPAmountModel(151.24), 35, NpsDate(2015,4,5), 1) should not be Some(SPContextMessage.ScenarioEight)
      }
    }
  }

}
