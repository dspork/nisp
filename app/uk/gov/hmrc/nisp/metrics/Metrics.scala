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

package uk.gov.hmrc.nisp.metrics

import com.codahale.metrics.Timer.Context
import com.codahale.metrics.{Counter, Histogram, Timer}
import uk.gov.hmrc.nisp.models.enums.APITypes.APITypes
import uk.gov.hmrc.nisp.models.enums.Exclusion._
import uk.gov.hmrc.nisp.models.enums.MQPScenario._
import uk.gov.hmrc.nisp.models.enums.Scenario.Scenario
import uk.gov.hmrc.nisp.models.enums.{Exclusion, MQPScenario, _}
import uk.gov.hmrc.play.graphite.MicroserviceMetrics

trait Metrics {
  def startTimer(api: APITypes): Timer.Context
  def startCitizenDetailsTimer(): Timer.Context
  def incrementFailedCounter(api: APITypes.APITypes): Unit
  
  def summary(forecast: BigDecimal, current: BigDecimal, contractedOut: Boolean, forecastOnly: Boolean, age: Int,
              forecastScenario: Scenario, personalMaximum: BigDecimal, yearsToContribute: Int, mqpScenario: Option[MQPScenario]): Unit
  def niRecord(gaps: Int, payableGaps: Int, pre75Years: Int, qualifyingYears: Int, yearsUntilSPA: Option[Int]): Unit
  def exclusion(exclusions: List[Exclusion]): Unit

  def cacheRead()
  def cacheReadFound()
  def cacheReadNotFound()
  def cacheWritten()
}

object Metrics extends Metrics with MicroserviceMetrics {
  val spSummaryCounter: Counter = metrics.defaultRegistry.counter("spsummary-counter")
  val niRecordCounter: Counter = metrics.defaultRegistry.counter("nirecord-counter")

  val timers = Map(
    APITypes.Summary -> metrics.defaultRegistry.timer("summary-response-timer"),
    APITypes.NIRecord -> metrics.defaultRegistry.timer("nirecord-response-timer"),
    APITypes.Liabilities -> metrics.defaultRegistry.timer("liabilities-response-timer"),
    APITypes.SchemeMembership -> metrics.defaultRegistry.timer("schememembership-response-timer")
  )

  val failedCounters = Map(
    APITypes.Summary -> metrics.defaultRegistry.counter("summary-failed-counter"),
    APITypes.NIRecord -> metrics.defaultRegistry.counter("nirecord-failed-counter"),
    APITypes.Liabilities -> metrics.defaultRegistry.counter("liabilities-failed-counter"),
    APITypes.SchemeMembership -> metrics.defaultRegistry.counter("schememembership-failed-counter")
  )

  override def startTimer(api: APITypes): Context = timers(api).time()
  override def incrementFailedCounter(api: APITypes): Unit = failedCounters(api).inc()

  val contractedOutMeter: Counter = metrics.defaultRegistry.counter("contracted-out")
  val notContractedOutMeter: Counter = metrics.defaultRegistry.counter("not-contracted-out")
  val forecastOnlyMeter: Counter = metrics.defaultRegistry.counter("forecast-only")
  val notForecastOnlyMeter: Counter = metrics.defaultRegistry.counter("not-forecast-only")
  val ageUpTo30: Counter = metrics.defaultRegistry.counter("age-upto-30")
  val age31To45: Counter = metrics.defaultRegistry.counter("age-31-to-45")
  val age46To55: Counter = metrics.defaultRegistry.counter("age-46-to-55")
  val age56To65: Counter = metrics.defaultRegistry.counter("age-56-to-65")
  val age66AndAbove: Counter = metrics.defaultRegistry.counter("age-66-and-above")
  val gapsMeter: Histogram = metrics.defaultRegistry.histogram("gaps")
  val payableGapsMeter: Histogram = metrics.defaultRegistry.histogram("payable-gaps")
  val pre75YearsMeter: Histogram = metrics.defaultRegistry.histogram("pre75-years")
  val qualifyingYearsMeter: Histogram = metrics.defaultRegistry.histogram("qualifying-years")
  val yearsUntilSPAMeter: Histogram = metrics.defaultRegistry.histogram("years-until-spa")

  val currentAmountMeter: Histogram = metrics.defaultRegistry.histogram("current-amount")
  val forecastAmountMeter: Histogram = metrics.defaultRegistry.histogram("forecast-amount")
  val personalMaxAmountMeter: Histogram = metrics.defaultRegistry.histogram("personal-maximum-amount")
  val yearsNeededToContribute: Histogram = metrics.defaultRegistry.histogram("years-needed-to-contribute")

  val forecastScenarioMeters: Map[Scenario, Counter] = Map(
    Scenario.Reached -> metrics.defaultRegistry.counter("forecastscenario-reached"),
    Scenario.ContinueWorkingMax -> metrics.defaultRegistry.counter("forecastscenario-continueworkingmax"),
    Scenario.ContinueWorkingNonMax -> metrics.defaultRegistry.counter("forecastscenario-continueworkingnonmax"),
    Scenario.FillGaps -> metrics.defaultRegistry.counter("forecastscenario-fillgaps"),
    Scenario.ForecastOnly -> metrics.defaultRegistry.counter("forecastscenario-forecastonly"),
    Scenario.CantGetPension -> metrics.defaultRegistry.counter("forecastscenario-cantgetpension")
  )

  val mqpScenarioMeters: Map[MQPScenario, Counter] = Map(
    MQPScenario.CantGet -> metrics.defaultRegistry.counter("mqpscenario-cantget"),
    MQPScenario.ContinueWorking -> metrics.defaultRegistry.counter("mqpscenario-continueworking"),
    MQPScenario.CanGetWithGaps -> metrics.defaultRegistry.counter("mqpscenario-cangetwithgaps")
  )

  val exclusionMeters: Map[Exclusion, Counter] = Map(
    Exclusion.Abroad -> metrics.defaultRegistry.counter("exclusion-abroad"),
    Exclusion.MarriedWomenReducedRateElection -> metrics.defaultRegistry.counter("exclusion-mwrre"),
    Exclusion.Dead -> metrics.defaultRegistry.counter("exclusion-dead"),
    Exclusion.IsleOfMan -> metrics.defaultRegistry.counter("exclusion-isle-of-man"),
    Exclusion.AmountDissonance -> metrics.defaultRegistry.counter("amount-dissonance"),
    Exclusion.PostStatePensionAge -> metrics.defaultRegistry.counter("exclusion-post-spa"),
    Exclusion.ManualCorrespondenceIndicator -> metrics.defaultRegistry.counter("exclusion-manual-correspondence")
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

  override def niRecord(gaps: Int, payableGaps: Int, pre75Years: Int, qualifyingYears: Int, yearsUntilSPA: Option[Int]): Unit = {
    gapsMeter.update(gaps)
    payableGapsMeter.update(payableGaps)
    pre75YearsMeter.update(pre75Years)
    qualifyingYearsMeter.update(qualifyingYears)
    yearsUntilSPA.foreach(yearsUntilSPAMeter.update)
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

  override def cacheRead(): Unit = metrics.defaultRegistry.meter("cache-read").mark()
  override def cacheReadFound(): Unit = metrics.defaultRegistry.meter("cache-read-found").mark()
  override def cacheReadNotFound(): Unit = metrics.defaultRegistry.meter("cache-read-not-found").mark()
  override def cacheWritten(): Unit = metrics.defaultRegistry.meter("cache-written").mark()
  override def startCitizenDetailsTimer(): Context = metrics.defaultRegistry.timer("citizen-details-timer").time()
}
