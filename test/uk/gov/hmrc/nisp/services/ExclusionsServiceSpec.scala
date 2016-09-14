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
import uk.gov.hmrc.nisp.models.ExclusionsModel
import uk.gov.hmrc.nisp.models.enums.Exclusion
import uk.gov.hmrc.nisp.models.enums.Exclusion.Exclusion
import uk.gov.hmrc.nisp.models.nps._
import uk.gov.hmrc.play.test.UnitSpec

class ExclusionsServiceSpec extends UnitSpec with OneAppPerSuite  {

  def createModelWithListItems(spExclusions: Exclusion *): ExclusionsModel = ExclusionsModel(spExclusions.toList)

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
  val noExclusions = ExclusionsModel(List())


  "getSPExclusions" when {
    "customer has no exclusions" should {
      "return None" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }
    }

    "checking Manual correspondence exclusion for a customer" when {
      "exclude customer if manual correspondence status is true" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", true).getSPExclusions shouldBe createModelWithListItems(Exclusion.ManualCorrespondenceIndicator)
      }
    }

    "checking for abroad exclusion" should {
      
      "return Abroad exclusion if customer is abroad and is male and their state pension age is before 06/10/2018" in {
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 5), "M", false)
          .getSPExclusions shouldBe createModelWithListItems(Exclusion.Abroad)
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 5), "m", false)
          .getSPExclusions shouldBe createModelWithListItems(Exclusion.Abroad)
      }

      "return no exclusions if customer is abroad and is male and their state pension age is on or after 06/10/2018" in {
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 6), "M", false).getSPExclusions shouldBe noExclusions
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 7), "M", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is abroad and is female and their state pension age is before 06/10/2018" in {
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 5), "F", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is abroad and is female and their state pension age is on or after 06/10/2018" in {
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 6), "F", false).getSPExclusions shouldBe noExclusions
        ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 7), "F", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is not abroad and is male and their state pension age is before 06/10/2018" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 5), "M", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is not abroad and is male and their state pension age is on or after 06/10/2018" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 6), "M", false).getSPExclusions shouldBe noExclusions
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 7), "M", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is not abroad and is female and their state pension age is before 06/10/2018" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 5), "F", false).getSPExclusions shouldBe noExclusions
      }

      "return no exclusions if customer is not abroad and is female and their state pension age is on or after 06/10/2018" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 6), "F", false).getSPExclusions shouldBe noExclusions
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, NpsDate(2018, 10, 7), "F", false).getSPExclusions shouldBe noExclusions
      }
    }

    "checking for customer MWRRE" should {
      "return no exclusions for non-mwrre customer" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }

      "return MWRRE exclusion for customer with MWRRE flag set" in {
        ExclusionsService(false, true, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.MarriedWomenReducedRateElection)
      }
    }

    "checking for customer SPA" should {
      "return no exclusions for customer with SPA 07/04/2016" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }
    }

    "checking for customer that is deceased" should {
      "return no exclusions for alive customer" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }

      "return Dead exclusion for dead customer" in {
        ExclusionsService(false, false, Some(NpsDate(2014, 1, 1)), nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.Dead)
      }
    }

    "checking for customer ever stayed in Isle of Man" should {
      "return IsleOfMan exclusion for liability type 5 with country code 'ISLE OF MAN'" in {
        ExclusionsService(false, false, None,
          nino, List(NpsLiability(5, None, None)), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.IsleOfMan)
      }

      "return no exclusion for nino starting with 'MA'" in {
        ExclusionsService(false, false, None,
          iomNino.value, List(), 100, 100, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }
    }

    "checking if there is any dissonance between provided and calculated values" should {
      "return no exclusions for 200 provided and 200 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 200, 200, defaultDate, statePensionAge, "F", false).getSPExclusions shouldBe noExclusions
      }

      "return dissonance exclusion for 200 provided and 201 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 200, 201, defaultDate, statePensionAge, "F", false)
          .getSPExclusions shouldBe createModelWithListItems(Exclusion.AmountDissonance)
      }

      "return dissonance exclusion for 199 provided and 200 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 199, 200, defaultDate, statePensionAge, "F", false)
          .getSPExclusions shouldBe createModelWithListItems(Exclusion.AmountDissonance)
      }
    }

    "checking if they have reached State Pension age -1 day on or after 6 April 2016" should {
      "return no exclusions for pre SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 08/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 8), "F", false).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 9), "F", false).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 9), "F", false).getSPExclusions shouldBe noExclusions
        }
      }
      "return no exclusions for pre SPa -1 and before 6 April 2016" when {
        "Man, SPA: 07/04/2016, Current Date: 05/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 7), "F", false).getSPExclusions shouldBe noExclusions
        }
        "Man, SPA: 07/04/2016, Current Date: 13/03/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 3, 13), NpsDate(2016, 4, 7), "F", false).getSPExclusions shouldBe noExclusions
        }

      }
      "return post SPa exclusion for post SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 06/04/2016, Current Date: 05/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 6), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
        "Man, SPA: 06/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 6), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
        "Man, SPA: 06/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 6), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
        "Man, SPA: 07/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 7), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
        "Man, SPA: 07/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 7), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
        "Man, SPA: 09/04/2016, Current Date: 08/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 8), NpsDate(2016, 4, 9), "F", false).getSPExclusions shouldBe createModelWithListItems(Exclusion.PostStatePensionAge)
        }
      }
    }
  }

  "getNIExclusions" when {
    "customer has no exclusions" should {
      "return None" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }
    }

    "checking Manual correspondence status for a customer" when {
      "exclude customer if manual correspondence status is true" in {
        ExclusionsService(false, false, None, "", List(), 0, 151.25, NpsDate(2015,1,1), NpsDate(2016,4,5), "F", true).getNIExclusions shouldBe createModelWithListItems(Exclusion.ManualCorrespondenceIndicator)
      }
    }

    "checking for customer location" should {
        "return no exclusions if customer is not abroad" in {
          ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
        }

        "return no exclusions if customer is abroad" in {
          ExclusionsService(true, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
        }
    }

    "checking for customer MWRRE" should {
      "return no exclusions for non-mwrre customer" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }

      "return MWRRE exclusion for customer with MWRRE flag set" in {
        ExclusionsService(false, true, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe createModelWithListItems(Exclusion.MarriedWomenReducedRateElection)
      }
    }

    "checking for customer DOB" should {
      "return no exclusions for customer with SPA of 07/04/2016" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }

      "return no exclusions for customer with SPA of 05/04/2016" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }
    }

    "checking for customer that is deceased" should {
      "return no exclusions for alive customer" in {
        ExclusionsService(false, false, None, nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }

      "return Dead exclusion for dead customer" in {
        ExclusionsService(false, false, Some(NpsDate(2014, 1, 1)), nino, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe createModelWithListItems(Exclusion.Dead)
      }
    }

    "checking for customer ever stayed in Isle of Man" should {
      "return IsleOfMan exclusion for liability type 5'" in {
        ExclusionsService(false, false, None,
          nino, List(NpsLiability(5, None, None)), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe createModelWithListItems(Exclusion.IsleOfMan)
      }

      "return no exclusion for nino starting with 'MA'" in {
        ExclusionsService(false, false, None,
          iomNino.value, List(), 100, 100, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }
    }

    "checking if there is any dissonance between provided and calculated values" should {
      "return no exclusions for 200 provided and 200 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 200, 200, defaultDate, statePensionAge, "F", false).getNIExclusions shouldBe noExclusions
      }

      "return no dissonance exclusion for 200 provided and 201 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 200, 201, defaultDate, statePensionAge, "F", false)
          .getNIExclusions shouldBe noExclusions
      }

      "return no dissonance exclusion for 199 provided and 200 calculated" in {
        ExclusionsService(false, false, None, nino, List(), 199, 200, defaultDate, statePensionAge, "F", false)
          .getNIExclusions shouldBe noExclusions
      }
    }

    "checking if they have reached State Pension age -1 day on or after 6 April 2016" should {
      "return no exclusions for pre SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 08/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 8), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 9), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 9), "F", false).getNIExclusions shouldBe noExclusions
        }
      }
      "return no exclusions for pre SPa -1 and before 6 April 2016" when {
        "Man, SPA: 07/04/2016, Current Date: 05/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 7), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 07/04/2016, Current Date: 13/03/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 3, 13), NpsDate(2016, 4, 7), "F", false).getNIExclusions shouldBe noExclusions
        }

      }
      "return no exclusions for post SPa -1 and on or after 6 April 2016" when {
        "Man, SPA: 06/04/2016, Current Date: 05/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 6), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 06/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 6), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 06/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 6), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 07/04/2016, Current Date: 06/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 6), NpsDate(2016, 4, 7), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 07/04/2016, Current Date: 07/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 7), NpsDate(2016, 4, 7), "F", false).getNIExclusions shouldBe noExclusions
        }
        "Man, SPA: 09/04/2016, Current Date: 08/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 8), NpsDate(2016, 4, 9), "F", false).getNIExclusions shouldBe noExclusions
        }
      }
      "return no exclusions for post SPa -1 and before 6 April 2016" when {
        "Man, SPA: 05/04/2016, Current Date: 05/04/2016" in {
          ExclusionsService(false, false, None, nino, List(), 201, 201, NpsDate(2016, 4, 5), NpsDate(2016, 4, 5), "F", false).getNIExclusions shouldBe noExclusions
        }
      }
    }

    "checking only TooOld Exclusion does not appear" when {
      "customer has TooOld and AmountDissonance" in {
        ExclusionsService(false, false, None, "", List(), 0, 151.25, NpsDate(2015,1,1), NpsDate(2016,4,5), "F", false).getNIExclusions shouldBe noExclusions
      }
    }

  }
}
