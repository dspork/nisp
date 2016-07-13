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

import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.services.{ForecastingService, StatePensionService}

class StubStatePensionService(localDate: LocalDate = DateTime.now.toLocalDate) extends StatePensionService {
  override val npsConnector: NpsConnector = StubNpsConnector
  override val metrics: Metrics = StubMetrics
  override val forecastingService: ForecastingService = StubForecastingService
  override val now: LocalDate = localDate
}

object StubStatePensionService extends StubStatePensionService(DateTime.now.toLocalDate)
