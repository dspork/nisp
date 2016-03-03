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

package uk.gov.hmrc.nisp.services

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder
import uk.gov.hmrc.nisp.models.SPExclusionsModel
import uk.gov.hmrc.nisp.models.enums.SPExclusion
import uk.gov.hmrc.nisp.models.enums.SPExclusion.SPExclusion
import uk.gov.hmrc.nisp.models.nps._
import uk.gov.hmrc.play.test.UnitSpec

class SPExclusionsServiceSpec extends UnitSpec with OneAppPerSuite  {

  def createModelWithListItems(spExclusions: SPExclusion *): SPExclusionsModel = SPExclusionsModel(spExclusions.toList)

  "SPExclusions" when {
    val nino = "regular"
    val iomNino = TestAccountBuilder.isleOfManNino
    val countryNotSpecified = 0
    val countryGB = 1
    val countryNI = 8
    val countryEngland = 114
    val countryScotland = 115
    val countryWales = 116
    val countryIsleOfMan = 7
    val defaultDate = NpsDate(2016, 2, 9)
    val statePensionAge = NpsDate(2016, 4, 7)

    val noExclusions = SPExclusionsModel(List())

    "customer has no exclusions" should {
      "return None" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }
    }

    "checking for qualifying years" should {
      "return no exclusions in list for a customer with 9" in {
        SPExclusionsService(9, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions in list for a customer with 10" in {
        SPExclusionsService(10, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions

      }
    }

    "checking for customer location" should {
      "return IsAbroad exclusion for customer in JERSEY" in {
        SPExclusionsService(30, 4, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Abroad)
      }

      "return IsAbroad exclusion for customer in MOZAMBIQUE" in {
        SPExclusionsService(30, 10, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Abroad)
      }

      "return no exclusions for customer in GREAT BRITAIN" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for customer in ENGLAND" in {
        SPExclusionsService(30, countryEngland, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for customer in SCOTLAND" in {
        SPExclusionsService(30, countryScotland, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for customer in WALES" in {
        SPExclusionsService(30, countryWales, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for customer in NORTHERN IRELAND" in {
        SPExclusionsService(30, countryNI, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for customer with not specified" in {
        SPExclusionsService(30, countryNotSpecified, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }
    }

    "checking for customer MWRRE" should {
      "return no exclusions for non-mwrre customer" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return MWRRE exclusion for customer with MWRRE start date" in {
        SPExclusionsService(30, countryGB, true, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.MWRRE)
      }

      "return MWRRE exclusion for customer with MWRRE start date and end date" in {
        SPExclusionsService(30, countryGB, true, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.MWRRE)
      }
    }

    "checking for customer DOB" should {
      "return no exclusions for female born on 6/4/1953" in {
        SPExclusionsService(30, countryGB, false, "F", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions for male born on 6/4/1951" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return CustomerTooOld for male born on 5/4/1951" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, NpsDate(2016, 4, 5)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
      }

      "return CustomerTooOld for female born on 5/4/1953" in {
        SPExclusionsService(30, countryGB, false, "F", List(), None, nino, List(), 100, 100, defaultDate, NpsDate(2016, 4, 5)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
      }

    }

    "checking for customer that is deceased" should {
      "return no exclusions for alive customer" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return Dead exclusion for dead customer" in {
        SPExclusionsService(30, countryGB, false, "M", List(), Some(NpsDate(2014, 1, 1)), nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Dead)
      }
    }

    "checking for customer ever stayed in Isle of Man" should {
      "return no exclusion having country code of Isle of Man('ISLE OF MAN')" in {
        SPExclusionsService(30, countryIsleOfMan, false, "M", List(), None,
          nino, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return IOM exclusion for liability type 5 with country code 'ISLE OF MAN'" in {
        SPExclusionsService(30, countryIsleOfMan, false, "M", List(), None,
          nino, List(NpsLiability(5, None, None)), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe createModelWithListItems(SPExclusion.IOM)
      }

      "return no exclusion for nino starting with 'MA'" in {
        SPExclusionsService(30, countryIsleOfMan, false, "M", List(), None,
          iomNino.value, List(), 100, 100, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }
    }

    "checking if there is any dissonance between provided and calculated values" should {
      "return no exclusions for 200 provided and 200 calculated" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 200, 200, defaultDate, statePensionAge).getSPExclusions shouldBe noExclusions
      }

      "return dissonance exclusion for 200 provided and 201 calculated" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 200, 201, defaultDate, statePensionAge)
          .getSPExclusions shouldBe createModelWithListItems(SPExclusion.AmountDissonance)
      }

      "return dissonance exclusion for 199 provided and 200 calculated" in {
        SPExclusionsService(30, countryGB, false, "M", List(), None, nino, List(), 199, 200, defaultDate, statePensionAge)
          .getSPExclusions shouldBe createModelWithListItems(SPExclusion.AmountDissonance)
      }
    }

    "checking if they have reached State Pension age -1 day on or after 6 April 2016" should {
      "return no exclusions for pre SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 08/04/2016, Current Date: 06/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 8)).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 06/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 9)).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 07/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 9)).getSPExclusions shouldBe noExclusions
        }
      }
      "return no exclusions for pre SPa -1 and before 6 April 2016" when {
        "Man, SPA: 07/04/2016, Current Date: 05/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 7)).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 07/04/2016, Current Date: 13/03/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 3, 13), NpsDate(2016, 4, 7)).getSPExclusions shouldBe noExclusions
        }

      }
      "return post SPa exclusion for post SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 06/04/2016, Current Date: 05/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 6)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
        "Man, SPA: 06/04/2016, Current Date: 06/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 6)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
        "Man, SPA: 06/04/2016, Current Date: 07/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 6)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
        "Man, SPA: 07/04/2016, Current Date: 06/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 7)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
        "Man, SPA: 07/04/2016, Current Date: 07/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 7)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
        "Man, SPA: 09/04/2016, Current Date: 08/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 8), NpsDate(2016, 4, 9)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.PostStatePensionAge)
        }
      }
      "return too old exclusion for post SPa -1 and before 6 April 2016" when {
        "Man, SPA: 05/04/2016, Current Date: 05/04/2016" in {
          SPExclusionsService(12, countryGB, false, "M", List(), None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 5)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
        }
      }
    }

    "checking only TooOld Exclusion appears" when {
      "customer has TooOld and AmountDissonance" in {
        SPExclusionsService(12, countryGB, false, "M", List(), None, "", List(), 0, 151.25, NpsDate(2015,1,1), NpsDate(2016,4,5)).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
      }
    }
  }
}
