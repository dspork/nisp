/*
 * Copyright 2017 HM Revenue & Customs
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

import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.nisp.cache.{LiabilitiesCacheModel, NationalInsuranceCacheModel, SchemeMembershipCacheModel, SummaryCacheModel}
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder._
import uk.gov.hmrc.nisp.helpers.{StubNpsConnector, TestAccountBuilder}
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.nps.{NpsPensionForecast, _}
import uk.gov.hmrc.nisp.services.CachingService
import uk.gov.hmrc.play.http.{BadGatewayException, HeaderCarrier, HttpGet, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class NpsConnectorSpec extends UnitSpec with ScalaFutures with MockitoSugar {

  "NpsConnector" when {
    "There is nothing in the cache" should {
      "return summary" in {
        val npsSummaryModelF = StubNpsConnector.connectToSummary(regularNino)(HeaderCarrier())
        npsSummaryModelF.nino shouldBe regularNino.value
      }

      "throw not found for summary on unknown NINO" in {
        val npsSummaryModelF = StubNpsConnector.connectToSummary(nonExistentNino)(HeaderCarrier())
        whenReady(npsSummaryModelF.failed) { ex =>
          ex shouldBe a[NotFoundException]
        }
      }

      "throw bad gateway for summary on NPS unavailable" in {
        val npsSummaryModelF = StubNpsConnector.connectToSummary(isleOfManNino)(HeaderCarrier())
        whenReady(npsSummaryModelF.failed) { ex =>
          ex shouldBe a[BadGatewayException]
        }
      }

      "return national insurance" in {
        val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(regularNino)(HeaderCarrier())
        npsNiRecordModelF.nino shouldBe regularNino.value
      }

      "throw not found for national insurance on unknown NINO" in {
        val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(nonExistentNino)(HeaderCarrier())
        whenReady(npsNiRecordModelF.failed) { ex =>
          ex shouldBe a[NotFoundException]
        }
      }

      "throw bad gaeway for national insurance on NPS unavailable" in {
        val npsNiRecordModelF = StubNpsConnector.connectToNIRecord(isleOfManNino)(HeaderCarrier())
        whenReady(npsNiRecordModelF.failed) { ex =>
          ex shouldBe a[BadGatewayException]
        }
      }

      "return liabilities" in {
        val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(regularNino)(HeaderCarrier())
        npsLiabilitiesF.length shouldBe 1
        npsLiabilitiesF(0).liabilityType shouldBe 6
      }

      "throw not found for liabilities on unknown NINO" in {
        val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(nonExistentNino)(HeaderCarrier())
        whenReady(npsLiabilitiesF.failed) { ex =>
          ex shouldBe a[NotFoundException]
        }
      }

      "throw bad gateway for liabilities on NPS unavailable" in {
        val npsLiabilitiesF = StubNpsConnector.connectToLiabilities(nonExistentNino)(HeaderCarrier())
        whenReady(npsLiabilitiesF.failed) { ex =>
          ex shouldBe a[NotFoundException]
        }
      }

      "return scheme membership" in {
        val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(TestAccountBuilder.excludedNino)(HeaderCarrier())
        npsSchemeMembershipsF.length shouldBe 2
        npsSchemeMembershipsF(0).startDate shouldBe NpsDate(1978, 4, 6)
        npsSchemeMembershipsF(0).endDate shouldBe Some(NpsDate(1979, 6, 30))
      }

      "throw not found for scheme membership on unknown NINO" in {
        val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(nonExistentNino)(HeaderCarrier())
        whenReady(npsSchemeMembershipsF.failed) { ex =>
          ex shouldBe a[NotFoundException]
        }
      }

      "throw bad gateway for scheme membership on NPS unavailable" in {
        val npsSchemeMembershipsF = StubNpsConnector.connectToSchemeMembership(isleOfManNino)(HeaderCarrier())
        whenReady(npsSchemeMembershipsF.failed) { ex =>
          ex shouldBe a[BadGatewayException]
        }
      }
    }
  }

  "The information is cached" should {

    val mockMetrics = mock[Metrics]
    val mockHttp = mock[HttpGet]

    val mockSummaryRepo = mock[CachingService[SummaryCacheModel, NpsSummaryModel]]
    val mockNIRepo = mock[CachingService[NationalInsuranceCacheModel, NpsNIRecordModel]]
    val mockLiabiltiesRepo = mock[CachingService[LiabilitiesCacheModel, NpsLiabilityContainer]]
    val mockSchemeRepo = mock[CachingService[SchemeMembershipCacheModel, NpsSchemeMembershipContainer]]

    val testSummaryModel = NpsSummaryModel(
      TestAccountBuilder.regularNino.toString(),
      None,
      1,
      NpsDate(1956, 4, 6),
      None,
      NpsDate(2020, 4, 5),
      30,
      2019,
      Some(1),
      Some(35),
      None,
      None,
      None,
      NpsDate(2015, 4, 5),
      None,
      0,
      "F",
      NpsStatePensionAmount(
        Some(155.65),
        None,
        Some(155.65),
        None,
        NpsAmountA2016(
          119.30,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          0
        ),
        NpsAmountB2016(
          Some(155.65),
          None
        )
      ),
      NpsPensionForecast(
        155.65,
        40,
        155.65,
        155.65
      )
    )

    when(mockSummaryRepo.findByNino(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(testSummaryModel)))

    val fakeConnector = new NpsConnector {
      override def metrics: Metrics = mockMetrics
      override def http: HttpGet = mockHttp

      override val nationalInsuranceRepository: CachingService[NationalInsuranceCacheModel, NpsNIRecordModel] = mockNIRepo
      override val schemeMembershipRepository: CachingService[SchemeMembershipCacheModel, NpsSchemeMembershipContainer] = mockSchemeRepo
      override val summaryRepository: CachingService[SummaryCacheModel, NpsSummaryModel] = mockSummaryRepo
      override val liabilitiesRepository: CachingService[LiabilitiesCacheModel, NpsLiabilityContainer] = mockLiabiltiesRepo

      override val serviceUrl: String = "/"
      override val serviceOriginatorId: String = "id"
      override val serviceOriginatorIdKey: String = "key"
    }

    "return summary" in {
      val npsSummaryModelF = fakeConnector.connectToSummary(regularNino)(HeaderCarrier())
      await(npsSummaryModelF) shouldBe testSummaryModel
    }

    "not call NPS" in {
      val npsSummaryModelF = fakeConnector.connectToSummary(regularNino)(HeaderCarrier())
      await(npsSummaryModelF) shouldBe testSummaryModel
      verify(fakeConnector.http, never()).GET(Matchers.any())(Matchers.any(), Matchers.any())
    }
  }
}
