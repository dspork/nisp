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

package uk.gov.hmrc.nisp.metrics

import com.codahale.metrics.Timer.Context
import com.codahale.metrics.{Counter, Timer}
import com.kenshoo.play.metrics.MetricsRegistry
import uk.gov.hmrc.nisp.models.enums.APITypes.APITypes
import uk.gov.hmrc.nisp.models.enums.SPContextMessage._
import uk.gov.hmrc.nisp.models.enums.SPExclusion._
import uk.gov.hmrc.nisp.models.enums.{APITypes, SPContextMessage, SPExclusion}

trait Metrics {
  def startTimer(api: APITypes): Timer.Context
  def incrementFailedCounter(api: APITypes.APITypes): Unit
  
  def summary(forecast: BigDecimal, current: BigDecimal, scenario: Option[SPContextMessage], contractedOut: Boolean, forecastOnly: Boolean, age: Int): Unit
  def niRecord(gaps: Int, payableGaps: Int, pre75Years: Int, qualifyingYears: Int, yearsUntilSPA: Int): Unit
  def exclusion(exclusions: List[SPExclusion]): Unit
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

  val failedCounters = Map(
    APITypes.Summary -> MetricsRegistry.defaultRegistry.counter("summary-failed-counter"),
    APITypes.NIRecord -> MetricsRegistry.defaultRegistry.counter("nirecord-failed-counter"),
    APITypes.Liabilities -> MetricsRegistry.defaultRegistry.counter("liabilities-failed-counter"),
    APITypes.SchemeMembership -> MetricsRegistry.defaultRegistry.counter("schememembership-failed-counter")
  )

  override def startTimer(api: APITypes): Context = timers(api).time()
  override def incrementFailedCounter(api: APITypes): Unit = failedCounters(api).inc()

  val contractedOutMeter = MetricsRegistry.defaultRegistry.counter("contracted-out")
  val notContractedOutMeter = MetricsRegistry.defaultRegistry.counter("not-contracted-out")
  val forecastOnlyMeter = MetricsRegistry.defaultRegistry.counter("forecast-only")
  val notForecastOnlyMeter = MetricsRegistry.defaultRegistry.counter("not-forecast-only")
  val ageUpTo30 = MetricsRegistry.defaultRegistry.counter("age-upto-30")
  val age31To45 = MetricsRegistry.defaultRegistry.counter("age-31-to-45")
  val age46To55 = MetricsRegistry.defaultRegistry.counter("age-46-to-55")
  val age56To65 = MetricsRegistry.defaultRegistry.counter("age-56-to-65")
  val age66AndAbove = MetricsRegistry.defaultRegistry.counter("age-66-and-above")

  val gapsMeter = MetricsRegistry.defaultRegistry.histogram("gaps")
  val payableGapsMeter = MetricsRegistry.defaultRegistry.histogram("payable-gaps")
  val pre75YearsMeter = MetricsRegistry.defaultRegistry.histogram("pre75-years")
  val qualifyingYearsMeter = MetricsRegistry.defaultRegistry.histogram("qualifying-years")
  val forecastAmountMeter = MetricsRegistry.defaultRegistry.histogram("forecast-amount")
  val currentAmountMeter = MetricsRegistry.defaultRegistry.histogram("current-amount")
  val yearsUntilSPAMeter = MetricsRegistry.defaultRegistry.histogram("years-until-spa")

  val scenarioMeters: Map[SPContextMessage, Counter] = Map(
    SPContextMessage.ScenarioOne -> MetricsRegistry.defaultRegistry.counter("scenario-1"),
    SPContextMessage.ScenarioTwo -> MetricsRegistry.defaultRegistry.counter("scenario-2"),
    SPContextMessage.ScenarioThree -> MetricsRegistry.defaultRegistry.counter("scenario-3"),
    SPContextMessage.ScenarioFour -> MetricsRegistry.defaultRegistry.counter("scenario-4"),
    SPContextMessage.ScenarioFive -> MetricsRegistry.defaultRegistry.counter("scenario-5"),
    SPContextMessage.ScenarioSix -> MetricsRegistry.defaultRegistry.counter("scenario-6"),
    SPContextMessage.ScenarioSeven -> MetricsRegistry.defaultRegistry.counter("scenario-7"),
    SPContextMessage.ScenarioEight -> MetricsRegistry.defaultRegistry.counter("scenario-8")
  )

  val exclusionMeters: Map[SPExclusion, Counter] = Map(
    SPExclusion.Abroad -> MetricsRegistry.defaultRegistry.counter("exclusion-abroad"),
    SPExclusion.MWRRE -> MetricsRegistry.defaultRegistry.counter("exclusion-mwrre"),
    SPExclusion.CustomerTooOld -> MetricsRegistry.defaultRegistry.counter("exclusion-too-old"),
    SPExclusion.ContractedOut -> MetricsRegistry.defaultRegistry.counter("exclusion-contracted-out"),
    SPExclusion.Dead -> MetricsRegistry.defaultRegistry.counter("exclusion-dead"),
    SPExclusion.IOM -> MetricsRegistry.defaultRegistry.counter("exclusion-isle-of-man"),
    SPExclusion.AmountDissonance -> MetricsRegistry.defaultRegistry.counter("amount-dissonance"),
    SPExclusion.PostStatePensionAge -> MetricsRegistry.defaultRegistry.counter("exclusion-post-spa")
  )

  override def summary(forecast: BigDecimal, current: BigDecimal, scenario: Option[SPContextMessage],
                       contractedOut: Boolean, forecastOnly: Boolean, age: Int): Unit = {
    forecastAmountMeter.update(forecast.toInt)
    currentAmountMeter.update(current.toInt)
    scenario.foreach(scenarioMeters(_).inc())
    if(contractedOut) contractedOutMeter.inc() else notContractedOutMeter.inc()
    if(forecastOnly) forecastOnlyMeter.inc() else notForecastOnlyMeter.inc()
    mapToAgeMeter(age)
  }

  override def niRecord(gaps: Int, payableGaps: Int, pre75Years: Int, qualifyingYears: Int, yearsUntilSPA: Int): Unit = {
    gapsMeter.update(gaps)
    payableGapsMeter.update(payableGaps)
    pre75YearsMeter.update(pre75Years)
    qualifyingYearsMeter.update(qualifyingYears)
    yearsUntilSPAMeter.update(yearsUntilSPA)
  }

  override def exclusion(exclusions: List[SPExclusion]): Unit = exclusions.foreach(exclusionMeters(_).inc())

  private def mapToAgeMeter(age: Int): Unit = {
    if (56 to 65 contains age)
      age56To65.inc()
    else if (age > 65)
      age66AndAbove.inc()
    else if (46 to 55 contains age)
      age46To55.inc()
    else if (31 to 45 contains age)
      age31To45.inc()
    else
      ageUpTo30.inc()
  }
}
