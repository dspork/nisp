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

import org.scalatest.Assertions
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.helpers.{StubForecastingService, TestAccountBuilder}
import uk.gov.hmrc.nisp.models.enums.{MQPScenario, Scenario}
import uk.gov.hmrc.nisp.models.nps.{NpsAmountA2016, NpsAmountB2016, NpsDate, NpsSchemeMembership}
import uk.gov.hmrc.nisp.models._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class ForecastingServiceSpec extends UnitSpec with OneAppPerSuite {
  // scalastyle:off magic.number

  "QYs to 2016" should {
    "return 2 for earnings included up to of 2014-04-05" in {
      StubForecastingService.qualifyingYearsTo2016(NpsDate(2014,4,5)) shouldBe 2
    }

    "return 1 for earnings included up to of 2015-04-05" in {
      StubForecastingService.qualifyingYearsTo2016(NpsDate(2015,4,5)) shouldBe 1
    }

    "return 0 for earnings included up to of 2016-04-05" in {
      StubForecastingService.qualifyingYearsTo2016(NpsDate(2016,4,5)) shouldBe 0
    }

    "return 0 for earnings included up to of 2017-04-05" in {
      StubForecastingService.qualifyingYearsTo2016(NpsDate(2017,4,5)) shouldBe 0
    }
  }

  "QYs at 2016" should {
    "return 32 for earnings included up to of 2014-04-05 and current QYs 30" in {
      StubForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 30) shouldBe 32
    }

    "return 2 for earnings included up to of 2014-04-05 and current QYs 0" in {
      StubForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 0) shouldBe 2
    }
  }

  "AP to accrue for 2016 forecast" should {
    "return 0 for earnings included up to 2017-04-05 and ap_amount 1" in {
      StubForecastingService.forecastAPToAccrue(NpsDate(2017,4,5), 1) shouldBe 0
    }

    "return 0 for earnings included up to 2016-04-05 and ap_amount 1" in {
      StubForecastingService.forecastAPToAccrue(NpsDate(2016,4,5), 1) shouldBe 0
    }

    "return 1 for earnings included up to 2015-04-05 and ap_amount 1" in {
      StubForecastingService.forecastAPToAccrue(NpsDate(2015,4,5), 1) shouldBe 1
    }

    "return 2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      StubForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1) shouldBe 2
    }

    "return 3.2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      StubForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1.6) shouldBe 3.2
    }
  }

  "Amount A at 2016" should {
    "return 0 for BP of 0, existing AP 0, forecasted AP 0" in {
      StubForecastingService.forecastAmountAAt2016(0, 0, 0) shouldBe 0
    }

    "return 119.30 for BP of 119.30, existing AP 0, forecasted AP 0" in {
      StubForecastingService.forecastAmountAAt2016(119.30, 0, 0) shouldBe 119.30
    }

    "return 129.30 for BP of 119.30, existing AP 10, forecasted AP 0" in {
      StubForecastingService.forecastAmountAAt2016(119.30, 10, 0) shouldBe 129.30
    }

    "return 131.10 for BP of 119.30, existing AP 10, forecasted AP 1.8" in {
      StubForecastingService.forecastAmountAAt2016(119.30, 10, 1.8) shouldBe 131.1
    }
  }

  "UAP capped earnings" should {
    "return 0 for 0 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(0) shouldBe 0
    }

    "return 40039 for 40039 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(40039) shouldBe 40039
    }

    "return 40040 for 40040 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(40040) shouldBe 40040
    }

    "return 40040 for 40041 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(40041) shouldBe 40040
    }

    "return 40040 for 40040.6 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(40040.6) shouldBe 40040
    }

    "return 34571.54 for 34571.54 earnings" in {
      StubForecastingService.cappedEarningsAtUAP(34571.54) shouldBe 34571.54
    }
  }

  "Band 2" should {
    "return 0 for 0 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 14999 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(14999, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 15000 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(15000, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 15001 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(15001, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 1.5 for 15001.5 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(15001.5, NpsDate(2014,4,5)) shouldBe 1.5
    }

    "return 15001.5 for 30001.5 earnings 2014-04-05 earnings included up to" in {
      StubForecastingService.band2(30001.5, NpsDate(2014,4,5)) shouldBe 15001.5
    }
  }

  "S2P Amount" should {
    "return FRAA (1.77) for band 2 £0" in {
      StubForecastingService.s2pAmount(NpsDate(2013,4,5), 0) shouldBe 1.77
    }
    
    "return FRAA (1.77) for band 2 £109.40" in {
      StubForecastingService.s2pAmount(NpsDate(2013,4,5), 109.40) shouldBe 1.77
    }

    "return FRAA + 0.01 (1.78) for band 2 £109.41" in {
      StubForecastingService.s2pAmount(NpsDate(2013,4,5), 109.41) shouldBe 1.78
    }

    "return FRAA + 0.11 (1.88) for band 2 £2501" in {
      StubForecastingService.s2pAmount(NpsDate(2013,4,5), 2501) shouldBe 1.88
    }

    "return 2.89 for 40040 earnings (band 2 25040)" in {
      StubForecastingService.s2pAmount(NpsDate(2014,4,5), 25040) shouldBe 2.89
    }
  }

  "Earnings above LEL" should {
    "return 0 for 0 earnings" in {
      StubForecastingService.earningsAboveLEL(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 5668 earnings" in {
      StubForecastingService.earningsAboveLEL(5668, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 5669 earnings" in {
      StubForecastingService.earningsAboveLEL(5669, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 0.01 for 5668.01 earnings" in {
      StubForecastingService.earningsAboveLEL(5668.01, NpsDate(2014,4,5)) shouldBe 0.01
    }
  }

  "S2P Deduction" should {
    "return 0 for 0 total earnings" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5),0,0,0) shouldBe 0
    }

    "return 1.4 for 40041 total earnings and 1.4 S2P Amount" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40041, 1.4) shouldBe 1.4
    }

    "return 0 for 40040 total earnings and 1.4 S2P Amount" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 0, 40040, 1.4) shouldBe 0
    }

    "return 1.4 for 40040.01 total earnings and 1.4 S2P Amount" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40040.01, 1.4) shouldBe 1.4
    }

    "return 0 for 54.70 earnings above LEL" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.70, 35000, 1.4) shouldBe 0
    }

    "return 0.01 for 54.71 earnings above LEL" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.71, 35000, 1.4) shouldBe 0.01
    }

    "return 1.83 for 20000 earnings above LEL" in {
      StubForecastingService.s2pDeduction(NpsDate(2013,4,5), 20000, 35000, 1.4) shouldBe 1.83
    }

    "return 0.73 for 8162 earnings above LEL for 2013/14" in {
      StubForecastingService.s2pDeduction(NpsDate(2014,4,5), 8162, 35000, 1.4) shouldBe 0.73
    }
  }

  "Qualifying Years to FRY" should {
    "return 0 for FRY 2015" in {
      StubForecastingService.qualifyingYearsToFRY(2015) shouldBe 0
    }

    "return 1 for FRY 2016" in {
      StubForecastingService.qualifyingYearsToFRY(2016) shouldBe 1
    }

    "return 2 for FRY 2017" in {
      StubForecastingService.qualifyingYearsToFRY(2017) shouldBe 2
    }
  }

  "Forecaste RDA at 2016" should {
    "return 0 for 0 existing RDA and 0 accrued RDA" in {
      StubForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 0) shouldBe 0
    }

    "return 10 for 10 existing RDA and 0 accrued RDA" in {
      StubForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 10) shouldBe 10
    }

    "return 10 for 0 existing RDA and 10 accrued RDA, earnings included up to 2014/15" in {
      StubForecastingService.forecastRDAAt2016(NpsDate(2015,4,5), 10, 0) shouldBe 10
    }

    "return 20 for 0 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      StubForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 0) shouldBe 20
    }

    "return 25 for 5 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      StubForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 5) shouldBe 25
    }
  }

  "Forecast Amount B at 2016" should {
    "return 3 for main amount 4, rebate derived amount 1" in {
      StubForecastingService.forecastAmountBAt2016(4,1)
    }
  }

  "Forecast Post 2016 New State Pension" should {
    "return 155.65 for starting amount 151.26, fry 2018, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2018, 151.26, 30, 1) shouldBe Forecast(155.65, 2, false)
    }

    "return 155.66 for starting amount 155.66, fry 2018, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2018, 155.66, 30, 1) shouldBe Forecast(155.66, 1, true)
    }

    "return 155.64 with 0 years for starting amount 155.64, fry 2015, qys 10" in {
      StubForecastingService.forecastPost2016StatePension(2015, 155.64, 10, 0) shouldBe Forecast(155.64, 0, false)
    }

    "return 0 with 0 years, for starting amount 155.64, fry 2015, qys 9" in {
      StubForecastingService.forecastPost2016StatePension(2015, 155.64, 9, 2) shouldBe Forecast(0, 0, false)
    }

    "return non-zero with 1 year, for starting amount 155.64, fry 2016, qys 9" in {
      StubForecastingService.forecastPost2016StatePension(2016, 155.64, 9, 1).amount should not be 0
      StubForecastingService.forecastPost2016StatePension(2016, 155.64, 9, 1) shouldBe Forecast(155.65, 2, false)
    }

    "return 155.65 with 1 year, for starting amount 155.64, fry 2016, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2016, 155.64, 30, 0) shouldBe Forecast(155.65, 1, false)
    }

    "return 155.64 with 1 year, for starting amount 151.19, fry 2016, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2016, 151.19, 30, 0) shouldBe Forecast(155.64, 1, false)
    }

    "return 155.64 with 2 years, for starting amount 146.75, fry 2017, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2017, 146.75, 30, 0) shouldBe Forecast(155.64, 2, false)
    }

    "return 153.34 with 3 years, for starting amount 140, fry 2018, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2018, 140, 30, 0) shouldBe Forecast(153.34, 3, false)
    }

    "return 155.65 with 4 years, for starting amount 140, fry 2019, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2019, 140, 30, 0) shouldBe Forecast(155.65, 4, false)
    }

    "return 155.65 with 4 years, for starting amount 140, fry 2020, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2020, 140, 30, 0) shouldBe Forecast(155.65, 4, false)
    }

    "return 155.65 with 4 years, for starting amount 140, fry 2021, qys 30" in {
      StubForecastingService.forecastPost2016StatePension(2021, 140, 30, 0) shouldBe Forecast(155.65, 4, false)

    }
    "return 155.65 with 4 years, for starting amount 140, fry 2021, qys 36" in {
      StubForecastingService.forecastPost2016StatePension(2021, 140, 36, 0) shouldBe Forecast(155.65, 4, false)
    }

    "return 155.65 with 5 for years for starting amount 137.86, fry 2021, qys 31" in {
      StubForecastingService.forecastPost2016StatePension(2021, (155.65/35)*31, 31, 1) shouldBe Forecast(155.65, 5, false)
    }

    "return 155.65 with 26 for years for starting amount 44.47, fry 2041, qys 9" in {
      StubForecastingService.forecastPost2016StatePension(2041, (155.65/35)*10, 10, 1) shouldBe Forecast(155.65, 26, false)
    }

    "return 155.65 with 26 for years for starting amount 4.447142857, fry 2099, qys 1" in {
      StubForecastingService.forecastPost2016StatePension(2099, 155.65/35, 1, 0) shouldBe Forecast(155.65, 34, false)
    }

    "return 155.65 with 26 for years for starting amount 8.89, fry 2099, qys 2" in {
      StubForecastingService.forecastPost2016StatePension(2099, (155.65/35)*2, 2, 0) shouldBe Forecast(155.65, 33, false)
    }


  }

  "Forecast Contracted Out" should {
    "return 154.87 for 10000 last year earnings, 2013/14 last year, 35QYs, 2015 FRY" in {
      StubForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2015,
        contractedOutLastYear = true
      ) shouldBe Forecast(154.87, 2, false)
    }

    "return 155.65 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY" in {
      StubForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe Forecast(155.65, 3, false)
    }

    "return 222.06 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      StubForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 100,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe Forecast(222.06, 2, true)
    }

    "return 145.98 for 10000 last year earnings, 2013/14 last year, 30QYs, 2016 FRY" in {
      StubForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 30,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe Forecast(145.98, 3, false)
    }
  }

  "Forecast" should {

  }

  def npsAmountA2016(ap: BigDecimal = 0.0): NpsAmountA2016 = NpsAmountA2016(119.30, Some(ap), None, None, None, None, None, None, 0.00)
  def npsAmountB2016: NpsAmountB2016 = NpsAmountB2016(Some(151.25), None)
  "getForecastAmount" should {

    val testNino = TestAccountBuilder.regularNino
    val hc = HeaderCarrier()

    "not return forecast amount 1234 with scheme membership that has ended" in {
      StubForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2010,3,5)), Some(NpsDate(2011,3,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino, 0, SPAmountModel(0))(hc).forecastAmount should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2012-04-04, earnings included up to 2013-04-05" in {
      StubForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,4)), Some(NpsDate(2012,4,4)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino, 0, SPAmountModel(0))(hc).forecastAmount should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-05, earnings included up to 2013-04-05" in {
      StubForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,3,5)), Some(NpsDate(2013,4,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino, 0, SPAmountModel(0))(hc).forecastAmount should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-06, earnings included up to 2013-04-05" in {
      StubForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,5)), Some(NpsDate(2013,4,6)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino, 0, SPAmountModel(0))(hc).forecastAmount should not be SPAmountModel(1234)
    }

    "return 53.31 for scheme membership in last tax year (not at end), 8 QYs" in {
      StubForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,5,5)), Some(NpsDate(2013,8,8)))
      ), NpsDate(2014,4,5), 8, npsAmountA2016(10), npsAmountB2016, 0, 2015, 0, 0, lastYearQualifying = true, testNino, 0, SPAmountModel(0))(hc).forecastAmount shouldBe SPAmountModel(53.31)
    }

    "return 222.06 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      StubForecastingService.getForecastAmount(
        List(NpsSchemeMembership(Some(NpsDate(2012,5,5)),Some(NpsDate(2014,8,8)))),
        NpsDate(2014,4,5), 35, npsAmountA2016(100), npsAmountB2016, 10000, 2016, 0, 0, lastYearQualifying = true, testNino,
      0, SPAmountModel(0))(hc).forecastAmount shouldBe SPAmountModel(222.06)
    }

    "return Reached Scenario, 155.65 with no years when 2013/14 last year, contracted in, 35, 2018 FRY" in {
      StubForecastingService.getForecastAmount(
        List(), NpsDate(2014, 4, 5), 35, npsAmountA2016(0), npsAmountB2016, 10000, 2018, 0, 0, lastYearQualifying = true, testNino,
        0, SPAmountModel(155.65))(hc) shouldBe ForecastingResult(SPAmountModel(155.65), 0, SPAmountModel(155.65), 0, Scenario.Reached, true)
    }

    "return ContinueWorkingMax Scenario 155.65 with 2 years when 2013/14 last year, contracted in, 33, 2018 FRY" in {
      StubForecastingService.getForecastAmount(
        List(), NpsDate(2014, 4, 5), 33, npsAmountA2016(0), npsAmountB2016, 10000, 2018, 0, 0, lastYearQualifying = true, testNino,
        0, SPAmountModel(146.76))(hc) shouldBe ForecastingResult(SPAmountModel(155.65), 2, SPAmountModel(155.65), 0, Scenario.ContinueWorkingMax, true)
    }

    "return ContinueWorkingNonMax Scenario 111.18 with 5 years when 2013/14 last year, contracted in, *, 2018 FRY" in {
      StubForecastingService.getForecastAmount(
        List(), NpsDate(2014, 4, 5), 20, npsAmountA2016(0), npsAmountB2016, 10000, 2018, 0, 0, lastYearQualifying = true, testNino,
        0, SPAmountModel(88.94))(hc) shouldBe ForecastingResult(SPAmountModel(111.18), 5, SPAmountModel(111.18), 0, Scenario.ContinueWorkingNonMax, false)
    }


    "return ContinueWorkingMax Scenario 111.18 Forecast, 120.07 Max  with 5 years when 2013/14 last year, contracted in, *, 2018 FRY" in {
      StubForecastingService.getForecastAmount(
        List(), NpsDate(2014, 4, 5), 20, npsAmountA2016(0), npsAmountB2016, 10000, 2018, 0, 0, lastYearQualifying = true, testNino,
        2, SPAmountModel(88.94))(hc) shouldBe ForecastingResult(SPAmountModel(111.18), 5, SPAmountModel(120.07), 2, Scenario.FillGaps, false)
    }

    "return Forecast Only Scenario 111.18 Forecast, 120.07 Max  with 5 years when 2013/14 last year, contracted in, *, 2018 FRY" in {
      StubForecastingService.getForecastAmount(
        List(NpsSchemeMembership(Some(NpsDate(2012,3,5)), Some(NpsDate(2014,4,6)))), NpsDate(2014, 4, 5), 35, npsAmountA2016(0), npsAmountB2016, 10000, 2015, 0, 0, lastYearQualifying = true, testNino,
        0, SPAmountModel(155.65))(hc) shouldBe ForecastingResult(SPAmountModel(154.87), 2, SPAmountModel(154.87), 0, Scenario.ForecastOnly, false)
    }

    StubForecastingService.forecast(
      earningsIncludedUpTo = NpsDate(2014,4,5),
      currentQualifyingYears = 35,
      existingRDA = 0,
      existingAP = 0,
      lastYearEarnings = 10000,
      finalRelevantYear = 2015,
      contractedOutLastYear = true
    ) shouldBe Forecast(154.87, 2, false)

  }

  "adjustForecast" should {
    "return 155.65 for forecast 2016 of 153.88" in {
      StubForecastingService.adjustForecast(0, 153.88, 0, 0) shouldBe 155.65
    }

    "not return 155.64 for forecast 2016 of 153.87" in {
      StubForecastingService.adjustForecast(0, 153.87, 0, 0) should not be 155.64
    }

    "return 1.77 for forecast 2016 of 153.87 and amountB 1.77" in {
      StubForecastingService.adjustForecast(0, 153.87, 0.01, 1.77) shouldBe 1.77
    }

    "return 10 for forecast amount 10, amountA 0.99, amountB 2.77" in {
      StubForecastingService.adjustForecast(10, 0, 0.99, 2.77) shouldBe 10
    }

    "return 11.77 for forecast amount 10, amountA 1, amountB 2.77" in {
      StubForecastingService.adjustForecast(10, 0, 1, 2.77) shouldBe 11.77
    }

    "return 155.65 for forecast amount 154.65, amountA 154.65, amountB 120" in {
      StubForecastingService.adjustForecast(154.65, 0, 154.65, 120) shouldBe 155.65
    }
  }

  def forecastingWithGaps(earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, existingRDA: BigDecimal, existingAP: BigDecimal,
                  lastYearEarnings: BigDecimal, finalRelevantYear: Int, contractedOutLastYear: Boolean): Int => BigDecimal = (fillableGaps: Int) =>
    StubForecastingService.forecast(earningsIncludedUpTo, currentQualifyingYears + fillableGaps, existingRDA, existingAP, lastYearEarnings, finalRelevantYear, contractedOutLastYear).amount

  "personalMaximumAmount" when {
    "the customer has already reached the maximum" should {

      "return 155.65 when a contracted in Amount B customer has more or equal to 35 years" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2015, 4, 5), 35, 0, 10, 20000, 2016, false)) shouldBe
          SPAmountModel(155.65)
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2015, 4, 5), 36, 0, 10, 20000, 2016, false)) shouldBe
          SPAmountModel(155.65)
      }

      "return 155.65 when a contracted in Amount B customer has more or equal to 35 years and 10 gaps" in {
        StubForecastingService.personalMaximum(10, forecastingWithGaps(NpsDate(2015, 4, 5), 36, 0, 10, 20000, 2014, false)) shouldBe
          SPAmountModel(155.65)
      }

      "return 155.65 when a contracted in Amount B customer has more or equal to 35 years and 10 gaps and retire in 2020" in {
        StubForecastingService.personalMaximum(10, forecastingWithGaps(NpsDate(2015, 4, 5), 36, 0, 10, 20000, 2019, false)) shouldBe
          SPAmountModel(155.65)
      }

      "return 155.65 when a contracted in Amount B customer has 20 years and 0 gaps and FRY is posted" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2016, 4, 5), 20, 0, 0, 20000, 2015, false)) shouldBe
          SPAmountModel(88.94)
      }

      "return 194.30 when a contracted in Amount A customer has 75 AP and 30 qualifying years in" in {
        StubForecastingService.personalMaximum(10, forecastingWithGaps(NpsDate(2016, 4, 5), 30, 0, 75, 20000, 2015, false)) shouldBe
          SPAmountModel(194.30)
      }

      "return 194.30 when a contracted in Amount A customer has 75 AP and 35 qualifying years in" in {
        StubForecastingService.personalMaximum(10, forecastingWithGaps(NpsDate(2016, 4, 5), 35, 0, 75, 20000, 2015, false)) shouldBe
          SPAmountModel(194.30)
      }

      "return 155.65 when a contracted in Amount A customer has more 20 years and 0 gaps and FRY is posted and 10 AP" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2016, 4, 5), 20, 0, 10, 20000, 2015, false)) shouldBe
          SPAmountModel(89.53)
      }

    }

    "the customer just needs to continue working" should {
      "return 155.65 when a contracted in Amount B customer has 20 years, 0 gaps and FRY is not posted 10 AP" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2015, 4, 5), 20, 0, 10, 20000, 2040, false)) shouldBe
          SPAmountModel(155.65)
      }

      "return 155.65 when a contracted in Amount B customer has 34 years, 0 gaps and FRY is not posted and 0 AP" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2015, 4, 5), 34, 0, 0, 20000, 2030, false)) shouldBe
          SPAmountModel(155.65)
      }

      "return 155.65 when a contracted in Amount B customer has 34 years, 0 gaps and FRY is not posted and 75 AP" in {
        StubForecastingService.personalMaximum(0, forecastingWithGaps(NpsDate(2015, 4, 5), 30, 0, 75, 20000, 2030, false)) shouldBe
          SPAmountModel(196.29)
      }

    }

    "the customer needs to fill gaps" when {
      "Amount B customer remains Amount B customer" should {
        "return 155.65 when customer has 25 Qualf Years, 3 years to FRY and 7 fillable gaps" in {
          StubForecastingService.personalMaximum(7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe
            SPAmountModel(155.65)
        }
        "return 155.65 when customer has 25 Qualf Years, 3 years to FRY and 6 fillable gaps" in {
          StubForecastingService.personalMaximum(6, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe
            SPAmountModel(151.20)
        }
        "return 155.65 when customer has 25 Qualf Years, 3 years to FRY and 8 fillable gaps" in {
          StubForecastingService.personalMaximum(8, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe
            SPAmountModel(155.65)
        }
      }

      "Amount A customer remains Amount A customer" should {
        "return 155.65 when customer has 25 Qualf Years, 3 years to FRY and 7 fillable gaps and 25 AP" in {
          StubForecastingService.personalMaximum(7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2017, false)) shouldBe
            SPAmountModel(156.07)
        }
        "return 155.65 when customer has 25 Qualf Years, 4 years to FRY and 7 fillable gaps and 25 AP" in {
          StubForecastingService.personalMaximum(7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2018, false)) shouldBe
            SPAmountModel(156.07)
        }
        "return 155.65 when customer has 25 Qualf Years, 5 years to FRY and 7 fillable gaps and 25 AP" in {
          StubForecastingService.personalMaximum(7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2019, false)) shouldBe
            SPAmountModel(156.07)
        }
      }

      "Amount A customer becomes Amount B customer" should {
        "return 155.65 when customer has 25 Qualf Years, 3 years to FRY and 7 fillable gaps and 25 AP" in {
          StubForecastingService.personalMaximum(7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 25, 10000, 2017, false)) shouldBe
            SPAmountModel(155.65)
        }
      }
    }
  }

  "personalMaximumScenario" when {
    "current == forecast == maximum" should {
      "return Reached" in {
        StubForecastingService.forecastScenario(SPAmountModel(155.65), SPAmountModel(155.65), SPAmountModel(155.65), 36) shouldBe Scenario.Reached
      }
    }

    "current < forecast == maximum in" should {
      "return ContinueWorkingMax when forecast == State Pension Max" in {
         StubForecastingService.forecastScenario(SPAmountModel(56), SPAmountModel(155.65), SPAmountModel(155.65), 30) shouldBe Scenario.ContinueWorkingMax
      }

      "return ContinueWorkingMax when forecast >= State Pension Max" in {
         StubForecastingService.forecastScenario(SPAmountModel(151), SPAmountModel(200.00), SPAmountModel(200), 30) shouldBe Scenario.ContinueWorkingMax
      }

      "return ContinueWorkingNonMax when Forecast is less than the State Pension Max" in {
        StubForecastingService.forecastScenario(SPAmountModel(56), SPAmountModel(155.64), SPAmountModel(155.64), 25) shouldBe Scenario.ContinueWorkingNonMax
      }
    }

    "current < forecast < gaps" should {
      "return FillGaps" in {
        StubForecastingService.forecastScenario(SPAmountModel(60), SPAmountModel(122), SPAmountModel(133), 25) shouldBe Scenario.FillGaps
      }
    }

    "current == forecast < gaps" should {
      "return FillGaps" in {
        StubForecastingService.forecastScenario(SPAmountModel(60), SPAmountModel(60), SPAmountModel(133), 25) shouldBe Scenario.FillGaps
      }
    }

    "current > forecast" should {
      "return ForecastOnly regardless of Max" in {
        StubForecastingService.forecastScenario(SPAmountModel(100), SPAmountModel(99), SPAmountModel(100), 35) shouldBe Scenario.ForecastOnly
        StubForecastingService.forecastScenario(SPAmountModel(100), SPAmountModel(99), SPAmountModel(99), 35) shouldBe Scenario.ForecastOnly
        StubForecastingService.forecastScenario(SPAmountModel(100), SPAmountModel(99), SPAmountModel(155.65), 35) shouldBe Scenario.ForecastOnly
      }
    }

    "yearsToQualify < 10" should {
      "return CantGetPension" in {
        StubForecastingService.forecastScenario(SPAmountModel(0), SPAmountModel(0), SPAmountModel(0), 5) shouldBe Scenario.CantGetPension
      }
    }

    "yearsToQualify = 10"  should {
      "return ForecastOnly" in {
        StubForecastingService.forecastScenario(SPAmountModel(100), SPAmountModel(99), SPAmountModel(100), 10) shouldBe Scenario.ForecastOnly
      }
    }

    "yearsToQualify > 10"  should {
      "return ForecastOnly" in {
        StubForecastingService.forecastScenario(SPAmountModel(100), SPAmountModel(99), SPAmountModel(100), 12) shouldBe Scenario.ForecastOnly
      }
    }
  }

  "minimumGapsToFillForPersonalMax" should {
    "Amount B customer remains Amount B customer" should {
      "return 7 when customer has 25 Qualf Years, 3 years to FRY and 7 fillable gaps" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(155.65, 7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe 7
      }
      "return 6 when customer has 25 Qualf Years, 3 years to FRY and 6 fillable gaps" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(151.20, 6, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe 6
      }
      "return 7 when customer has 25 Qualf Years, 3 years to FRY and 8 fillable gaps" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(155.65, 8, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false)) shouldBe 7
      }
    }

    "Amount A customer remains Amount A customer" should {
      "return 4 when customer has 25 Qualf Years, 3 years to FRY and 7 fillable gaps and 25 AP" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(156.07, 7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2017, false)) shouldBe 4
      }
      "return 4 when customer has 25 Qualf Years, 4 years to FRY and 7 fillable gaps and 25 AP" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(156.07, 7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2018, false)) shouldBe 4

      }
      "return 4 when customer has 25 Qualf Years, 5 years to FRY and 7 fillable gaps and 25 AP" in {
        StubForecastingService.minimumGapsToFillForPersonalMax(156.07, 7, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 35, 10000, 2019, false)) shouldBe 4
      }
    }

    "throw illegal argument exception when fillable gaps is less than 0" in {
      intercept[IllegalArgumentException] {
        StubForecastingService.minimumGapsToFillForPersonalMax(155.65, 0, forecastingWithGaps(NpsDate(2015,4,5), 25, 0, 0, 20000, 2017, false))
      }
    }

    "return 0 when fillable gaps are not needed" in {
      StubForecastingService.minimumGapsToFillForPersonalMax(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 25, 0, 0, 10000, 2024, false)) shouldBe 0
    }

    "return 1 when working only gets 34 years" in {
      StubForecastingService.minimumGapsToFillForPersonalMax(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 25, 0, 0, 10000, 2023, false)) shouldBe 1
    }


  }

  "MQP Scenario" should {
    "return Cant Get in MQP" in {
      val spResponseMQP = StubForecastingService.getMqpScenario(5, 3, 0)
      spResponseMQP shouldBe Some(MQPScenario.CantGet)
    }
    "return Continue Working in MQP" in {
      val spResponseMQP = StubForecastingService.getMqpScenario(5, 5, 0)
      spResponseMQP shouldBe Some(MQPScenario.ContinueWorking)
    }
    "return Can Get With Gaps in MQP" in {
      val spResponseMQP = StubForecastingService.getMqpScenario(5, 0, 5)
      spResponseMQP shouldBe Some(MQPScenario.CanGetWithGaps)
    }
    "return Can't Get in MQP with 0 current years, 3 years to work and 5 fillable gaps" in {
      val spResponseMQP =   StubForecastingService.getMqpScenario(0, 3, 5)
      spResponseMQP shouldBe Some(MQPScenario.CantGet)
    }
    "return Can Get With Gaps in MQP with 5 current years, 2 years to work and 4 fillable gaps" in {
      val spResponseMQP =   StubForecastingService.getMqpScenario(5, 2, 4)
      spResponseMQP shouldBe Some(MQPScenario.CanGetWithGaps)
    }
    "return Continue Working in MQP with 6 current years, 6 years to work and 4 fillable gaps" in {
      val spResponseMQP =   StubForecastingService.getMqpScenario(6, 6, 4)
      spResponseMQP shouldBe Some(MQPScenario.ContinueWorking)
    }
  }

  "modellingOptions" should {
    "throw exception when fillable gaps is 0" in {
      val ex = intercept[IllegalArgumentException] {
        StubForecastingService.modellingOptions(0, 0, forecastingWithGaps(NpsDate(2015, 4, 5), 25, 0, 0, 10000, 2023, false)) shouldBe 0
      }

      ex.getMessage shouldBe "requirement failed"
    }

    "return 155.65 and 1 when the customer only gets 34 years through forecasting" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 25, 0, 0, 10000, 2023, false)) shouldBe Seq(ModellingOption(1, SPAmountModel(155.65)))
    }

    "return an Empty Seq when the personal maximum has already been reached without gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 26, 0, 0, 10000, 2023, false)) shouldBe Seq(ModellingOption(1, SPAmountModel(155.65)))
    }

    "return (1, 151.20), (2, 155.65) when the customer needs to fill 2 gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 24, 0, 0, 10000, 2023, false)) shouldBe Seq(ModellingOption(1, SPAmountModel(151.20)), ModellingOption(2, SPAmountModel(155.65)))
    }

    "return (1, 146.76), (2, 151.20), (3, 155.65) when the customer needs to fill 3 gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 23, 0, 0, 10000, 2023, false)) shouldBe Seq(ModellingOption(1, SPAmountModel(146.76)), ModellingOption(2, SPAmountModel(151.20)), ModellingOption(3, SPAmountModel(155.65)))
    }

    "return (1, 142.31), (2, 146.76), (3, 151.20), (4, 155.65) when the customer needs to fill 4 gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 22, 0, 0, 10000, 2023, false)) shouldBe Seq(ModellingOption(1, SPAmountModel(142.31)), ModellingOption(2, SPAmountModel(146.76)), ModellingOption(3, SPAmountModel(151.2)), ModellingOption(4, SPAmountModel(155.65)))
    }

    "return (1, 115.63), (2, 120.07), (3, 124.52), (4, 128.97), (5, 133.41), (6, 137.86), (7, 142.31), (8, 146.76), (9, 151.20), (10, 155.65) when the customer needs to fill 10 gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 16, 0, 0, 10000, 2023, false)) shouldBe Seq(
        ModellingOption(1, SPAmountModel(115.63)),
        ModellingOption(2, SPAmountModel(120.07)),
        ModellingOption(3, SPAmountModel(124.52)),
        ModellingOption(4, SPAmountModel(128.97)),
        ModellingOption(5, SPAmountModel(133.41)),
        ModellingOption(6, SPAmountModel(137.86)),
        ModellingOption(7, SPAmountModel(142.31)),
        ModellingOption(8, SPAmountModel(146.76)),
        ModellingOption(9, SPAmountModel(151.2)),
        ModellingOption(10, SPAmountModel(155.65))
      )
    }

    "return (1, 115.63), (2, 120.07), (3, 124.52), (4, 128.97), (5, 133.41), (6, 137.86), (7, 142.31), (8, 146.76), (9, 151.20), (10, 155.65) when the customer needs to fill 11 gaps but only has 10 fillable gaps" in {
      StubForecastingService.modellingOptions(155.65, 10, forecastingWithGaps(NpsDate(2015, 4, 5), 15, 0, 0, 10000, 2023, false)) shouldBe Seq(
        ModellingOption(1, SPAmountModel(111.18)),
        ModellingOption(2, SPAmountModel(115.63)),
        ModellingOption(3, SPAmountModel(120.07)),
        ModellingOption(4, SPAmountModel(124.52)),
        ModellingOption(5, SPAmountModel(128.97)),
        ModellingOption(6, SPAmountModel(133.41)),
        ModellingOption(7, SPAmountModel(137.86)),
        ModellingOption(8, SPAmountModel(142.31)),
        ModellingOption(9, SPAmountModel(146.76)),
        ModellingOption(10, SPAmountModel(151.2))
      )
    }

  }
}
