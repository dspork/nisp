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
import SPExclusion.SPExclusion
import uk.gov.hmrc.nisp.models.enums.SPExclusion
import uk.gov.hmrc.nisp.models.nps._
import uk.gov.hmrc.play.test.UnitSpec

class SPExclusionsServiceSpec extends UnitSpec with OneAppPerSuite  {

  def createModelWithListItems(spExclusions: SPExclusion *): Option[SPExclusionsModel] = Some(SPExclusionsModel(spExclusions.toList))

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

    "customer has no exclusions" should {
      "return None" in {
        SPExclusionsService(30, countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }
    }

    "checking for qualifying years" should {
      "return no exclusions in list for a customer with 9" in {
        SPExclusionsService(9,  countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions in list for a customer with 10" in {
        SPExclusionsService(10,  countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None

      }
    }

    "checking for customer location" should {
      "return IsAbroad exclusion for customer in JERSEY" in {
        SPExclusionsService(30, 4, false, "MALE", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Abroad)
      }

      "return IsAbroad exclusion for customer in MOZAMBIQUE" in {
        SPExclusionsService(30, 10, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Abroad)
      }

      "return no exclusions for customer in GREAT BRITAIN" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for customer in ENGLAND" in {
        SPExclusionsService(30, countryEngland, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for customer in SCOTLAND" in {
        SPExclusionsService(30, countryScotland, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for customer in WALES" in {
        SPExclusionsService(30, countryWales, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for customer in NORTHERN IRELAND" in {
        SPExclusionsService(30, countryNI, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for customer with not specified" in {
        SPExclusionsService(30, countryNotSpecified, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }
    }

    "checking for customer MWRRE" should {
      "return no exclusions for non-mwrre customer" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return MWRRE exclusion for customer with MWRRE start date" in {
        SPExclusionsService(30,  countryGB, true, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.MWRRE)
      }

      "return MWRRE exclusion for customer with MWRRE start date and end date" in {
        SPExclusionsService(30,  countryGB, true, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.MWRRE)
      }
    }

    "checking for customer DOB" should {
      "return no exclusions for female born on 6/4/1953" in {
        SPExclusionsService(30,  countryGB, false, "F", NpsDate(1953,4,6), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return no exclusions for male born on 6/4/1951" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1951,4,6), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return CustomerTooOld for male born on 5/4/1951" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1951,4,5), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
      }

      "return CustomerTooOld for female born on 5/4/1953" in {
        SPExclusionsService(30,  countryGB, false, "F", NpsDate(1953,4,5), List(), None, nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.CustomerTooOld)
      }

    }

    "checking for customer that is deceased" should {
      "return no exclusions for alive customer" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1959,1,1), List(), None, nino, List()).getSPExclusions shouldBe None
      }

      "return Dead exclusion for dead customer" in {
        SPExclusionsService(30,  countryGB, false, "M", NpsDate(1959,1,1), List(), Some(NpsDate(2014,1,1)), nino, List()).getSPExclusions shouldBe createModelWithListItems(SPExclusion.Dead)
      }
    }

    "checking for customer ever stayed in Isle of Man" should {
      "return no exclusion having country code of Isle of Man('ISLE OF MAN')" in {
        SPExclusionsService(30,  countryIsleOfMan, false, "M", NpsDate(1959,1,1), List(), None,
          nino, List()).getSPExclusions shouldBe None
      }

      "return IOM exclusion for liability type 5 with country code 'ISLE OF MAN'" in {
        SPExclusionsService(30, countryIsleOfMan, false, "M", NpsDate(1959, 1, 1), List(), None,
          nino, List(NpsLiability(5, None, None))).getSPExclusions shouldBe createModelWithListItems(SPExclusion.IOM)
      }

      "return no exclusion for nino starting with 'MA'" in{
        SPExclusionsService(30,  countryIsleOfMan, false, "M", NpsDate(1959,1,1), List(), None,
          iomNino.value, List()).getSPExclusions shouldBe None
      }
    }
  }

}
