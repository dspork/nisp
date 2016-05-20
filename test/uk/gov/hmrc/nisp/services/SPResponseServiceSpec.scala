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

import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.helpers.{MockNpsConnector, MockSPResponseService, TestAccountBuilder}
import uk.gov.hmrc.nisp.models.enums.{Exclusion, MQPScenario}
import uk.gov.hmrc.nisp.models.nps._
import uk.gov.hmrc.nisp.models.{SPAgeModel, SPAmountModel}
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

class SPResponseServiceSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {
  val nino = TestAccountBuilder.regularNino
  val exclusionNino = TestAccountBuilder.excludedNino
  val nonexistentnino = TestAccountBuilder.nonExistentNino

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "return an SPSummaryModel object for present NINO" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.isEmpty shouldBe false
  }

  "return failed Future for a non-existent NINO" in {
    val spResponse = MockSPResponseService.getSPResponse(nonexistentnino)
    ScalaFutures.whenReady(spResponse.failed) { ex =>
      ex shouldBe a[NotFoundException]
    }
  }

  "return an SPSummaryModel object with the correct NINO" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.nino shouldBe nino.value
  }

  "return an SPSummaryModel object with the correct last processed date" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.lastProcessedDate shouldBe NpsDate(new LocalDate(2014, 4, 5))
  }

  "return an SPSummaryModel object with the correct SPAmount" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.statePensionAmount shouldBe SPAmountModel(118.24)
  }

  "return an SPSummaryModel object with isAbroad" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.isAbroad shouldBe(false)
  }

  "return an SPSummaryModel object with the correct SPAge" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.statePensionAge shouldBe SPAgeModel(65, NpsDate(new LocalDate(2017, 11, 21)))
  }

  "return a SPSummaryModel and an ExclusionsModel" in {
    val spResponse = MockSPResponseService.getSPResponse(exclusionNino)
    spResponse.spSummary.get.dateOfBirth shouldBe NpsDate(1952, 11, 21)
    spResponse.spExclusions.isEmpty shouldBe false
    spResponse.niExclusions.isEmpty shouldBe false
  }

  "return an ExclusionModel with correct exclusion" in {
    val spResponse = MockSPResponseService.getSPResponse(exclusionNino)
    spResponse.spExclusions.get.exclusions shouldBe List(Exclusion.Abroad, Exclusion.MWRRE)
    spResponse.niExclusions.get.exclusions shouldBe List(Exclusion.MWRRE)
  }

  "age Calculation" should {
    "return 30 age for date of Birth 10/05/1980 and current date 10/7/2010" in {
      new MockSPResponseService(localDate = new LocalDate(2010, 7, 10)).getAge(NpsDate(1980, 5, 10)) shouldBe 30
    }

    "return 9 age for date of Birth 10/05/2001 and current date 10/7/2010" in {
      new MockSPResponseService(localDate = new LocalDate(2010, 7, 10)).getAge(NpsDate(2001, 5, 10)) shouldBe 9
    }

    "return 59 age for date of Birth 19/07/1950 and current date 10/7/2010" in {
      new MockSPResponseService(localDate = new LocalDate(2010, 7, 10)).getAge(NpsDate(1950, 7, 19)) shouldBe 59
    }

    "return 50 age for date of Birth 10/07/1950 and current date 10/7/2000" in {
      new MockSPResponseService(localDate = new LocalDate(2000, 7, 10)).getAge(NpsDate(1950, 7, 10)) shouldBe 50
    }

    "return 49 age for date of Birth 10/07/1950 and current date 11/7/1999" in {
      new MockSPResponseService(localDate = new LocalDate(1999, 7, 11)).getAge(NpsDate(1950, 7, 10)) shouldBe 49
    }

    "return 48 age for date of Birth 10/07/1950 and current date 9/7/1999" in {
      new MockSPResponseService(localDate = new LocalDate(1999, 7, 9)).getAge(NpsDate(1950, 7, 10)) shouldBe 48
    }
  }

  "return None in MQP scenario, for regular NINO" in {
    val spResponse = MockSPResponseService.getSPResponse(nino)
    spResponse.spSummary.get.mqp shouldBe None
  }

  "MQP Scenario" should {
    "return Cant Get in MQP" in {
      val spResponseMQP = MockSPResponseService.getMqpScenario(5, 3, 0)
      spResponseMQP shouldBe Some(MQPScenario.CantGet)
    }
    "return Continue Working in MQP" in {
      val spResponseMQP = MockSPResponseService.getMqpScenario(5, 5, 0)
      spResponseMQP shouldBe Some(MQPScenario.ContinueWorking)
    }
    "return Can Get With Gaps in MQP" in {
      val spResponseMQP = MockSPResponseService.getMqpScenario(5, 0, 5)
      spResponseMQP shouldBe Some(MQPScenario.CanGetWithGaps)
    }
    "return Can't Get in MQP with 0 current years, 3 years to work and 5 fillable gaps" in {
      val spResponseMQP =   MockSPResponseService.getMqpScenario(0, 3, 5)
      spResponseMQP shouldBe Some(MQPScenario.CantGet)
    }
    "return Can Get With Gaps in MQP with 5 current years, 2 years to work and 4 fillable gaps" in {
      val spResponseMQP =   MockSPResponseService.getMqpScenario(5, 2, 4)
      spResponseMQP shouldBe Some(MQPScenario.CanGetWithGaps)
    }
    "return Continue Working in MQP with 6 current years, 6 years to work and 4 fillable gaps" in {
      val spResponseMQP =   MockSPResponseService.getMqpScenario(6, 6, 4)
      spResponseMQP shouldBe Some(MQPScenario.ContinueWorking)
    }
  }

}
