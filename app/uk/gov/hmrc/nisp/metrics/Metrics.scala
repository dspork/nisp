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
import uk.gov.hmrc.nisp.models.SPForecastModel
import uk.gov.hmrc.nisp.models.enums.APITypes.APITypes
import uk.gov.hmrc.nisp.models.enums.Exclusion._
import uk.gov.hmrc.nisp.models.enums.MQPScenario._
import uk.gov.hmrc.nisp.models.enums.Scenario.Scenario
import uk.gov.hmrc.nisp.models.enums.{Exclusion, MQPScenario, _}

trait Metrics {
  def startTimer(api: APITypes): Timer.Context
  def startCitizenDetailsTimer(): Timer.Context
  def incrementFailedCounter(api: APITypes.APITypes): Unit
  
  def summary(forecast: BigDecimal, current: BigDecimal, contractedOut: Boolean, forecastOnly: Boolean, age: Int,
              forecastScenario: Scenario, personalMaximum: BigDecimal, yearsToContribute: Int, mqpScenario: Option[MQPScenario]): Unit
  def niRecord(gaps: Int, payableGaps: Int, pre75Years: Int, qualifyingYears: Int, yearsUntilSPA: Int): Unit
  def exclusion(exclusions: List[Exclusion]): Unit

  def cacheRead()
  def cacheReadFound()
  def cacheReadNotFound()
  def cacheWritten()
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
  val yearsUntilSPAMeter = MetricsRegistry.defaultRegistry.histogram("years-until-spa")

  val currentAmountMeter = MetricsRegistry.defaultRegistry.histogram("current-amount")
  val forecastAmountMeter = MetricsRegistry.defaultRegistry.histogram("forecast-amount")
  val personalMaxAmountMeter = MetricsRegistry.defaultRegistry.histogram("personal-maximum-amount")
  val yearsNeededToContribute = MetricsRegistry.defaultRegistry.histogram("years-needed-to-contribute")

  val forecastScenarioMeters: Map[Scenario, Counter] = Map(
    Scenario.Reached -> MetricsRegistry.defaultRegistry.counter("forecastscenario-reached"),
    Scenario.ContinueWorkingMax -> MetricsRegistry.defaultRegistry.counter("forecastscenario-continueworkingmax"),
    Scenario.ContinueWorkingNonMax -> MetricsRegistry.defaultRegistry.counter("forecastscenario-continueworkingnonmax"),
    Scenario.FillGaps -> MetricsRegistry.defaultRegistry.counter("forecastscenario-fillgaps"),
    Scenario.ForecastOnly -> MetricsRegistry.defaultRegistry.counter("forecastscenario-forecastonly"),
    Scenario.CantGetPension -> MetricsRegistry.defaultRegistry.counter("forecastscenario-cantgetpension")
  )

  val mqpScenarioMeters: Map[MQPScenario, Counter] = Map(
    MQPScenario.CantGet -> MetricsRegistry.defaultRegistry.counter("mqpscenario-cantget"),
    MQPScenario.ContinueWorking -> MetricsRegistry.defaultRegistry.counter("mqpscenario-continueworking"),
    MQPScenario.CanGetWithGaps -> MetricsRegistry.defaultRegistry.counter("mqpscenario-cangetwithgaps")
  )

  val exclusionMeters: Map[Exclusion, Counter] = Map(
    Exclusion.Abroad -> MetricsRegistry.defaultRegistry.counter("exclusion-abroad"),
    Exclusion.MarriedWomenReducedRateElection -> MetricsRegistry.defaultRegistry.counter("exclusion-mwrre"),
    Exclusion.CustomerTooOld -> MetricsRegistry.defaultRegistry.counter("exclusion-too-old"),
    Exclusion.ContractedOut -> MetricsRegistry.defaultRegistry.counter("exclusion-contracted-out"),
    Exclusion.Dead -> MetricsRegistry.defaultRegistry.counter("exclusion-dead"),
    Exclusion.IsleOfMan -> MetricsRegistry.defaultRegistry.counter("exclusion-isle-of-man"),
    Exclusion.AmountDissonance -> MetricsRegistry.defaultRegistry.counter("amount-dissonance"),
    Exclusion.PostStatePensionAge -> MetricsRegistry.defaultRegistry.counter("exclusion-post-spa"),
    Exclusion.ManualCorrespondenceIndicator -> MetricsRegistry.defaultRegistry.counter("exclusion-manual-correspondence")
  )

  override def summary(forecast: BigDecimal, current: BigDecimal, contractedOut: Boolean,
                       forecastOnly: Boolean, age: Int, forecastScenario: Scenario, personalMaximum: BigDecimal,
                       yearsToContribute: Int, mqpScenario : Option[MQPScenario]): Unit = {
    forecastAmountMeter.update(forecast.toInt)
    currentAmountMeter.update(current.toInt)
    personalMaxAmountMeter.update(personalMaximum.toInt)
    forecastScenarioMeters(forecastScenario).inc()
    mqpScenario.foreach(mqpScenarioMeters(_).inc())
    yearsNeededToContribute.update(yearsToContribute)
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

  override def exclusion(exclusions: List[Exclusion]): Unit = exclusions.foreach(exclusionMeters(_).inc())

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

  override def cacheRead(): Unit = MetricsRegistry.defaultRegistry.meter("cache-read").mark
  override def cacheReadFound(): Unit = MetricsRegistry.defaultRegistry.meter("cache-read-found").mark
  override def cacheReadNotFound(): Unit = MetricsRegistry.defaultRegistry.meter("cache-read-not-found").mark
  override def cacheWritten(): Unit = MetricsRegistry.defaultRegistry.meter("cache-written").mark
  override def startCitizenDetailsTimer(): Context = MetricsRegistry.defaultRegistry.timer("citizen-details-timer").time()
}
