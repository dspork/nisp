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

package uk.gov.hmrc.nisp.metrics

import com.codahale.metrics.Timer
import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.MetricsRegistry

import java.util.concurrent.TimeUnit

import uk.gov.hmrc.nisp.models.enums.APITypes
import uk.gov.hmrc.nisp.models.enums.APITypes
import uk.gov.hmrc.nisp.models.enums.APITypes.APITypes

trait Metrics {
  def startTimer(api: APITypes): Timer.Context
  def incrementSuccessCounter(api: APITypes.APITypes): Unit
  def incrementFailedCounter(api: APITypes.APITypes): Unit
}

object Metrics extends Metrics {

  val spSummaryCounter = MetricsRegistry.defaultRegistry.counter("spsummary-counter")
  val niRecordCounter = MetricsRegistry.defaultRegistry.counter("nirecord-counter")

  val timers = Map(
    APITypes.Summary -> MetricsRegistry.defaultRegistry.timer("summary-response-timer"),
    APITypes.NIRecord -> MetricsRegistry.defaultRegistry.timer("nirecord-response-timer"),
    APITypes.Liabilities -> MetricsRegistry.defaultRegistry.timer("liabilities-response-timer"),
    APITypes.SchemeMembership -> MetricsRegistry.defaultRegistry.timer("schememembership-response-timer")
  )

  val successCounters = Map(
    APITypes.Summary -> MetricsRegistry.defaultRegistry.counter("summary-success-counter"),
    APITypes.NIRecord -> MetricsRegistry.defaultRegistry.counter("nirecord-success-counter"),
    APITypes.Liabilities -> MetricsRegistry.defaultRegistry.counter("liabilities-success-counter"),
    APITypes.SchemeMembership -> MetricsRegistry.defaultRegistry.counter("schememembership-success-counter")
  )

  val failedCounters = Map(
    APITypes.Summary -> MetricsRegistry.defaultRegistry.counter("summary-failed-counter"),
    APITypes.NIRecord -> MetricsRegistry.defaultRegistry.counter("nirecord-failed-counter"),
    APITypes.Liabilities -> MetricsRegistry.defaultRegistry.counter("liabilities-failed-counter"),
    APITypes.SchemeMembership -> MetricsRegistry.defaultRegistry.counter("schememembership-failed-counter")
  )

  override def startTimer(api: APITypes): Context = timers(api).time()

  override def incrementSuccessCounter(api: APITypes): Unit = successCounters(api).inc()

  override def incrementFailedCounter(api: APITypes): Unit = failedCounters(api).inc()

}
