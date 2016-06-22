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
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder._
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, StubNpsConnector}
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.http.{BadGatewayException, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

class NpsConnectorSpec extends UnitSpec with ScalaFutures {

  "return summary" in {
    val npsSummaryModelF = StubNpsConnector.connectToSummary(regularNino)(HeaderCarrier())
    npsSummaryModelF.nino shouldBe regularNino.value
  }

  "throw not found for summary on unknown NINO" in {
    val npsSummaryModelF = StubNpsConnector.connectToSummary(nonExistentNino)(HeaderCarrier())
    whenReady(npsSummaryModelF.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }
  
  "throw bad gateway for summary on NPS unavailable" in {
    val npsSummaryModelF = StubNpsConnector.connectToSummary(isleOfManNino)(HeaderCarrier())
    whenReady(npsSummaryModelF.failed) {ex =>
      ex shouldBe a [BadGatewayException]
    }
  }

  "return national insurance" in {
    val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(regularNino)(HeaderCarrier())
    npsNiRecordModelF.nino shouldBe regularNino.value
  }

  "throw not found for national insurance on unknown NINO" in {
    val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(nonExistentNino)(HeaderCarrier())
    whenReady(npsNiRecordModelF.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "throw bad gaeway for national insurance on NPS unavailable" in {
    val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(isleOfManNino)(HeaderCarrier())
    whenReady(npsNiRecordModelF.failed) {ex =>
      ex shouldBe a [BadGatewayException]
    }
  }

  "return liabilities" in {
    val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(regularNino)(HeaderCarrier())
    npsLiabilitiesF.length shouldBe 1
    npsLiabilitiesF(0).liabilityType shouldBe 6
  }

  "throw not found for liabilities on unknown NINO" in {
    val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(nonExistentNino)(HeaderCarrier())
    whenReady(npsLiabilitiesF.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "throw bad gateway for liabilities on NPS unavailable" in {
    val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(nonExistentNino)(HeaderCarrier())
    whenReady(npsLiabilitiesF.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "return scheme membership" in {
    val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(TestAccountBuilder.excludedNino)(HeaderCarrier())
    npsSchemeMembershipsF.length shouldBe 2
    npsSchemeMembershipsF(0).startDate shouldBe Some(NpsDate(1978, 4, 6))
    npsSchemeMembershipsF(0).endDate shouldBe Some(NpsDate(1979, 6, 30))
  }

  "throw not found for scheme membership on unknown NINO" in {
    val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(nonExistentNino)(HeaderCarrier())
    whenReady(npsSchemeMembershipsF.failed) {ex =>
      ex shouldBe a [NotFoundException]
    }
  }

  "throw bad gateway for scheme membership on NPS unavailable" in {
    val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(isleOfManNino)(HeaderCarrier())
    whenReady(npsSchemeMembershipsF.failed) {ex =>
      ex shouldBe a [BadGatewayException]
    }
  }
}
