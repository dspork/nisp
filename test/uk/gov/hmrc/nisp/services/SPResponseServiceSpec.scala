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

import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockNpsConnector}
import uk.gov.hmrc.nisp.models.enums.SPExclusion
import uk.gov.hmrc.nisp.models.nps._
import uk.gov.hmrc.nisp.models.{SPAgeModel, SPAmountModel}
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

class SPResponseServiceSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {
  val nino = TestAccountBuilder.regularNino
  val exclusionNino = TestAccountBuilder.excludedNino
  val nonexistentnino = TestAccountBuilder.nonExistentNino

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def testSPServiceWithMockHttp(localDate: LocalDate = new LocalDate()): SPResponseService = new SPResponseService {
    override def nps: NpsConnector = MockNpsConnector
    override def now: LocalDate = localDate
  }

  "return an SPSummaryModel object for present NINO" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nino)
    spResponse.spSummary.isEmpty shouldBe false
  }

  "return failed Future for a non-existent NINO" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nonexistentnino)
    ScalaFutures.whenReady(spResponse.failed) { ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "return an SPSummaryModel object with the correct NINO" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nino)
    spResponse.spSummary.get.nino shouldBe nino.value
  }

  "return an SPSummaryModel object with the correct last processed date" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nino)
    spResponse.spSummary.get.lastProcessedDate shouldBe NpsDate(new LocalDate(2014,4,5))
  }

  "return an SPSummaryModel object with the correct SPAmount" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nino)
    spResponse.spSummary.get.statePensionAmount shouldBe SPAmountModel(118.24)
  }

  "return an SPSummaryModel object with the correct SPAge" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(nino)
    spResponse.spSummary.get.statePensionAge shouldBe SPAgeModel(65,NpsDate(new LocalDate(2017,11,21)))
  }

  "return no SPSummaryModel and a SPExclusionsModel" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(exclusionNino)
    spResponse.spSummary shouldBe None
    spResponse.spExclusions.isEmpty shouldBe false
  }

  "return an SPExclusionModel with correct exclusion" in {
    val spResponse = testSPServiceWithMockHttp().getSPResponse(exclusionNino)
    spResponse.spExclusions.get.spExclusions shouldBe List(SPExclusion.Abroad)
  }

  "age Calculation" should {
    "return 30 age for date of Birth 10/05/1980 and current date 10/7/2010" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(2010,7,10)).getAge(NpsDate(1980, 5, 10)) shouldBe 30
    }

    "return 9 age for date of Birth 10/05/2001 and current date 10/7/2010" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(2010,7,10)).getAge(NpsDate(2001, 5, 10)) shouldBe 9
    }

    "return 59 age for date of Birth 19/07/1950 and current date 10/7/2010" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(2010,7,10)).getAge(NpsDate(1950, 7, 19)) shouldBe 59
    }

    "return 50 age for date of Birth 10/07/1950 and current date 10/7/2000" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(2000,7,10)).getAge(NpsDate(1950, 7, 10)) shouldBe 50
    }

    "return 49 age for date of Birth 10/07/1950 and current date 11/7/1999" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(1999,7,11)).getAge(NpsDate(1950, 7, 10)) shouldBe 49
    }

    "return 48 age for date of Birth 10/07/1950 and current date 9/7/1999" in {
      testSPServiceWithMockHttp(localDate = new LocalDate(1999,7,9)).getAge(NpsDate(1950, 7, 10)) shouldBe 48
    }
  }
}
