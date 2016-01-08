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

package uk.gov.hmrc.nisp.connectors

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockNpsConnector}
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

class NpsConnectorSpec extends UnitSpec with ScalaFutures {

  val nino = TestAccountBuilder.regularNino
  val excludedNino = TestAccountBuilder.excludedNino
  val nonExistentNino = TestAccountBuilder.nonExistentNino

  "return SUMMARY object on request with correct NINO" in {
    val summaryResult = MockNpsConnector.connectToSummary(nino)(HeaderCarrier())
    summaryResult.nino shouldBe nino.value
  }

  "return NOT_FOUND from SUMMARY feed for unknown NINO" in {
    val summaryResult = MockNpsConnector.connectToSummary(nonExistentNino)(HeaderCarrier())
    whenReady(summaryResult.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "return NI Record object on request with correct NINO" in {
    val niRecordResult = MockNpsConnector.connectToNIRecord(nino)(HeaderCarrier())
    niRecordResult.nino shouldBe nino.value
  }

  "return NOT_FOUND from NI Record feed for unknown NINO" in {
    val niRecordResult = MockNpsConnector.connectToNIRecord(nonExistentNino)(HeaderCarrier())
    whenReady(niRecordResult.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "return Liabilities on request with correct NINO" in {
    val liabilities = MockNpsConnector.connectToLiabilities(nino)(HeaderCarrier())
    liabilities.length shouldBe 1
    liabilities(0).liabilityType shouldBe 6
  }

  "return NOT_FOUND from Liabilities feed for unknown NINO" in {
    val liabilities = MockNpsConnector.connectToLiabilities(nonExistentNino)(HeaderCarrier())
    whenReady(liabilities.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "return Scheme Memberships on request with correct NINO" in {
    val schemes = MockNpsConnector.connectToSchemeMembership(excludedNino)(HeaderCarrier())
    schemes.length shouldBe 2
    schemes(0).startDate shouldBe Some(NpsDate(1978, 4, 6))
    schemes(0).endDate shouldBe Some(NpsDate(1979, 6, 30))
  }

  "return NOT_FOUND from SchemeMemberships feed for unknown NINO" in {
    val schemes = MockNpsConnector.connectToSchemeMembership(nonExistentNino)(HeaderCarrier())
    whenReady(schemes.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }
}
