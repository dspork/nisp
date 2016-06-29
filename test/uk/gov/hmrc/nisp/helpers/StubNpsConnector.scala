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

package uk.gov.hmrc.nisp.helpers

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.nisp.cache.{LiabilitiesCacheModel, NationalInsuranceCacheModel, SchemeMembershipCacheModel, SummaryCacheModel}
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.nps.{NpsLiabilityContainer, NpsNIRecordModel, NpsSchemeMembershipContainer, NpsSummaryModel}
import uk.gov.hmrc.nisp.services.CachingService
import uk.gov.hmrc.play.http.HttpGet

import scala.concurrent.Future

object StubNpsConnector extends NpsConnector with MockitoSugar {
  override def http: HttpGet = StubNPSHttp.stubHttp
  override val serviceUrl: String = ""
  override val metrics: Metrics = StubMetrics
  override val serviceOriginatorIdKey: String = "key"
  override val serviceOriginatorId: String = "id"

  override val summaryRepository: CachingService[SummaryCacheModel, NpsSummaryModel] = mock[CachingService[SummaryCacheModel, NpsSummaryModel]]
  override val nationalInsuranceRepository: CachingService[NationalInsuranceCacheModel, NpsNIRecordModel] = mock[CachingService[NationalInsuranceCacheModel, NpsNIRecordModel]]
  override val schemeMembershipRepository: CachingService[SchemeMembershipCacheModel, NpsSchemeMembershipContainer] = mock[CachingService[SchemeMembershipCacheModel, NpsSchemeMembershipContainer]]
  override val liabilitiesRepository: CachingService[LiabilitiesCacheModel, NpsLiabilityContainer] = mock[CachingService[LiabilitiesCacheModel, NpsLiabilityContainer]]

  when(StubNpsConnector.summaryRepository.findByNino(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  when(StubNpsConnector.nationalInsuranceRepository.findByNino(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  when(StubNpsConnector.liabilitiesRepository.findByNino(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  when(StubNpsConnector.schemeMembershipRepository.findByNino(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
}
