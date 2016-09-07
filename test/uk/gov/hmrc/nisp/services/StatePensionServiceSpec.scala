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

package uk.gov.hmrc.nisp.services

import org.joda.time.{DateTime, LocalDate}
import org.mockito.Matchers
import org.scalatest.EitherValues
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.helpers._
import uk.gov.hmrc.nisp.metrics.Metrics
import uk.gov.hmrc.nisp.models.enums.{Exclusion, Scenario}
import uk.gov.hmrc.nisp.models.{StatePension, StatePensionAmount, StatePensionAmounts, StatePensionExclusion}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

class StatePensionServiceSpec extends UnitSpec with OneAppPerSuite with EitherValues with MockitoSugar {
  val nino = TestAccountBuilder.regularNino
  val exclusionNino = TestAccountBuilder.excludedNino
  val nonexistentnino = TestAccountBuilder.nonExistentNino


  val regularTestData: StatePension = StatePension(
    earningsIncludedUpTo = new LocalDate(2014, 4, 5),
    amounts = StatePensionAmounts(
      protectedPayment = false,
      current = StatePensionAmount(None, None, 118.24),
      forecast = StatePensionAmount(Some(3), None, 137.19),
      maximum = StatePensionAmount(Some(3), Some(0), 137.19),
      cope = StatePensionAmount(None, None, 0)
    ),
    pensionAge = 65,
    pensionDate = new LocalDate(2017, 11, 21),
    finalRelevantYear = 2016,
    numberOfQualifyingYears = 27,
    pensionSharingOrder = false,
    currentFullWeeklyPensionAmount = 155.65
  )

  val exclusionTestData: StatePensionExclusion = StatePensionExclusion(
    exclusionReasons = List(Exclusion.MarriedWomenReducedRateElection, Exclusion.Abroad),
    pensionAge = 65,
    pensionDate = new LocalDate(2017, 11, 21)
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getStatement" should {
    "return a State Pension Statement for a regular Nino and it should be the correct information" in {
      val statePension: Either[StatePensionExclusion, StatePension] = StubStatePensionService.getStatement(nino)
      statePension.isLeft shouldBe false
      statePension.right.value shouldBe regularTestData
    }

    "return a Exclusion Message and some information for the excluded Nino" in {
      val statePension: Either[StatePensionExclusion, StatePension] = StubStatePensionService.getStatement(exclusionNino)
      statePension.isRight shouldBe false
      statePension.left.value shouldBe exclusionTestData
    }

    "Log a StatePension Metric" in {
      val stub = new StatePensionService {
        override def now: LocalDate = new LocalDate(2016, 7, 14)
        override val npsConnector: NpsConnector = StubNpsConnector
        override val metrics: Metrics = mock[Metrics]
        override val forecastingService: ForecastingService =  StubForecastingService
        override val citizenDetailsService: CitizenDetailsService = StubCitizenDetailsService
      }

      await(stub.getStatement(nino))
      verify(stub.metrics, times(1)).summary(
        Matchers.eq[BigDecimal](137.19),
        Matchers.eq[BigDecimal](118.24),
        Matchers.eq(true),
        Matchers.eq(false),
        Matchers.eq(63),
        Matchers.eq(Scenario.ContinueWorkingNonMax),
        Matchers.eq[BigDecimal](137.19),
        Matchers.eq(3),
        Matchers.eq(None)
      )
    }

    "Log a StatePension Exclusion Metric" in {
      val stub = new StatePensionService {
        override def now: LocalDate = new LocalDate(2016, 7, 14)
        override val npsConnector: NpsConnector = StubNpsConnector
        override val metrics: Metrics = mock[Metrics]
        override val forecastingService: ForecastingService =  StubForecastingService
        override val citizenDetailsService: CitizenDetailsService = StubCitizenDetailsService
      }

      await(stub.getStatement(exclusionNino))
      verify(stub.metrics, never).summary(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      verify(stub.metrics, times(1)).exclusion(
        Matchers.eq(List(Exclusion.MarriedWomenReducedRateElection, Exclusion.Abroad))
      )
    }
  }
}
