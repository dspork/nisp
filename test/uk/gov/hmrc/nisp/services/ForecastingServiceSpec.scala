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

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.CustomAuditConnector
import uk.gov.hmrc.nisp.helpers.{MockForecastingService, TestAccountBuilder, MockCustomAuditConnector}
import uk.gov.hmrc.nisp.models.SPAmountModel
import uk.gov.hmrc.nisp.models.nps.{NpsSchemeMembership, NpsAmountB2016, NpsAmountA2016, NpsDate}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class ForecastingServiceSpec extends UnitSpec with OneAppPerSuite {
  "QYs to 2016" should {
    "return 2 for earnings included up to of 2014-04-05" in {
      MockForecastingService.qualifyingYearsTo2016(NpsDate(2014,4,5)) shouldBe 2
    }

    "return 1 for earnings included up to of 2015-04-05" in {
      MockForecastingService.qualifyingYearsTo2016(NpsDate(2015,4,5)) shouldBe 1
    }

    "return 0 for earnings included up to of 2016-04-05" in {
      MockForecastingService.qualifyingYearsTo2016(NpsDate(2016,4,5)) shouldBe 0
    }

    "return 0 for earnings included up to of 2017-04-05" in {
      MockForecastingService.qualifyingYearsTo2016(NpsDate(2017,4,5)) shouldBe 0
    }
  }

  "QYs at 2016" should {
    "return 32 for earnings included up to of 2014-04-05 and current QYs 30" in {
      MockForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 30) shouldBe 32
    }

    "return 2 for earnings included up to of 2014-04-05 and current QYs 0" in {
      MockForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 0) shouldBe 2
    }
  }

  "AP to accrue for 2016 forecast" should {
    "return 0 for earnings included up to 2017-04-05 and ap_amount 1" in {
      MockForecastingService.forecastAPToAccrue(NpsDate(2017,4,5), 1) shouldBe 0
    }

    "return 0 for earnings included up to 2016-04-05 and ap_amount 1" in {
      MockForecastingService.forecastAPToAccrue(NpsDate(2016,4,5), 1) shouldBe 0
    }

    "return 1 for earnings included up to 2015-04-05 and ap_amount 1" in {
      MockForecastingService.forecastAPToAccrue(NpsDate(2015,4,5), 1) shouldBe 1
    }

    "return 2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      MockForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1) shouldBe 2
    }

    "return 3.2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      MockForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1.6) shouldBe 3.2
    }
  }

  "Amount A at 2016" should {
    "return 0 for BP of 0, existing AP 0, forecasted AP 0" in {
      MockForecastingService.forecastAmountAAt2016(0, 0, 0) shouldBe 0
    }

    "return 119.30 for BP of 119.30, existing AP 0, forecasted AP 0" in {
      MockForecastingService.forecastAmountAAt2016(119.30, 0, 0) shouldBe 119.30
    }

    "return 129.30 for BP of 119.30, existing AP 10, forecasted AP 0" in {
      MockForecastingService.forecastAmountAAt2016(119.30, 10, 0) shouldBe 129.30
    }

    "return 131.10 for BP of 119.30, existing AP 10, forecasted AP 1.8" in {
      MockForecastingService.forecastAmountAAt2016(119.30, 10, 1.8) shouldBe 131.1
    }
  }

  "UAP capped earnings" should {
    "return 0 for 0 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(0) shouldBe 0
    }

    "return 40039 for 40039 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(40039) shouldBe 40039
    }

    "return 40040 for 40040 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(40040) shouldBe 40040
    }

    "return 40040 for 40041 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(40041) shouldBe 40040
    }

    "return 40040 for 40040.6 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(40040.6) shouldBe 40040
    }

    "return 34571.54 for 34571.54 earnings" in {
      MockForecastingService.cappedEarningsAtUAP(34571.54) shouldBe 34571.54
    }
  }

  "Band 2" should {
    "return 0 for 0 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 14999 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(14999, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 15000 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(15000, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 15001 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(15001, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 1.5 for 15001.5 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(15001.5, NpsDate(2014,4,5)) shouldBe 1.5
    }

    "return 15001.5 for 30001.5 earnings 2014-04-05 earnings included up to" in {
      MockForecastingService.band2(30001.5, NpsDate(2014,4,5)) shouldBe 15001.5
    }
  }

  "S2P Amount" should {
    "return FRAA (1.77) for band 2 £0" in {
      MockForecastingService.s2pAmount(NpsDate(2013,4,5), 0) shouldBe 1.77
    }
    
    "return FRAA (1.77) for band 2 £109.40" in {
      MockForecastingService.s2pAmount(NpsDate(2013,4,5), 109.40) shouldBe 1.77
    }

    "return FRAA + 0.01 (1.78) for band 2 £109.41" in {
      MockForecastingService.s2pAmount(NpsDate(2013,4,5), 109.41) shouldBe 1.78
    }

    "return FRAA + 0.11 (1.88) for band 2 £2501" in {
      MockForecastingService.s2pAmount(NpsDate(2013,4,5), 2501) shouldBe 1.88
    }

    "return 2.89 for 40040 earnings (band 2 25040)" in {
      MockForecastingService.s2pAmount(NpsDate(2014,4,5), 25040) shouldBe 2.89
    }
  }

  "Earnings above LEL" should {
    "return 0 for 0 earnings" in {
      MockForecastingService.earningsAboveLEL(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 5668 earnings" in {
      MockForecastingService.earningsAboveLEL(5668, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 5669 earnings" in {
      MockForecastingService.earningsAboveLEL(5669, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 0.01 for 5668.01 earnings" in {
      MockForecastingService.earningsAboveLEL(5668.01, NpsDate(2014,4,5)) shouldBe 0.01
    }
  }

  "S2P Deduction" should {
    "return 0 for 0 total earnings" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5),0,0,0) shouldBe 0
    }

    "return 1.4 for 40041 total earnings and 1.4 S2P Amount" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40041, 1.4) shouldBe 1.4
    }

    "return 0 for 40040 total earnings and 1.4 S2P Amount" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 0, 40040, 1.4) shouldBe 0
    }

    "return 1.4 for 40040.01 total earnings and 1.4 S2P Amount" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40040.01, 1.4) shouldBe 1.4
    }

    "return 0 for 54.70 earnings above LEL" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.70, 35000, 1.4) shouldBe 0
    }

    "return 0.01 for 54.71 earnings above LEL" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.71, 35000, 1.4) shouldBe 0.01
    }

    "return 1.83 for 20000 earnings above LEL" in {
      MockForecastingService.s2pDeduction(NpsDate(2013,4,5), 20000, 35000, 1.4) shouldBe 1.83
    }

    "return 0.73 for 8162 earnings above LEL for 2013/14" in {
      MockForecastingService.s2pDeduction(NpsDate(2014,4,5), 8162, 35000, 1.4) shouldBe 0.73
    }
  }

  "Qualifying Years to FRY" should {
    "return 0 for FRY 2015" in {
      MockForecastingService.qualifyingYearsToFRY(2015) shouldBe 0
    }

    "return 1 for FRY 2016" in {
      MockForecastingService.qualifyingYearsToFRY(2016) shouldBe 1
    }

    "return 2 for FRY 2017" in {
      MockForecastingService.qualifyingYearsToFRY(2017) shouldBe 2
    }
  }

  "Forecaste RDA at 2016" should {
    "return 0 for 0 existing RDA and 0 accrued RDA" in {
      MockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 0) shouldBe 0
    }

    "return 10 for 10 existing RDA and 0 accrued RDA" in {
      MockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 10) shouldBe 10
    }

    "return 10 for 0 existing RDA and 10 accrued RDA, earnings included up to 2014/15" in {
      MockForecastingService.forecastRDAAt2016(NpsDate(2015,4,5), 10, 0) shouldBe 10
    }

    "return 20 for 0 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      MockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 0) shouldBe 20
    }

    "return 25 for 5 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      MockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 5) shouldBe 25
    }
  }

  "Forecast Amount B at 2016" should {
    "return 3 for main amount 4, rebate derived amount 1" in {
      MockForecastingService.forecastAmountBAt2016(4,1)
    }
  }

  "Forecast Post 2016 New State Pension" should {
    "return 155.66 for starting amount 151.26, fry 2018, qys 30" in {
      MockForecastingService.forecastPost2016StatePension(2018, 155.66, 30) shouldBe 155.66
    }

    "return 155.64 for starting amount 155.64, fry 2015, qys 10" in {
      MockForecastingService.forecastPost2016StatePension(2015, 155.64, 10) shouldBe 155.64
    }

    "return 0 for starting amount 155.64, fry 2015, qys 9" in {
      MockForecastingService.forecastPost2016StatePension(2015, 155.64, 9) shouldBe 0
    }

    "return non-zero for starting amount 155.64, fry 2016, qys 9" in {
      MockForecastingService.forecastPost2016StatePension(2016, 155.64, 9) should not be 0
    }

    "return 155.65 for starting amount 155.64, fry 2016, qys 30" in {
      MockForecastingService.forecastPost2016StatePension(2016, 155.64, 30) shouldBe 155.65
    }

    "return 155.64 for starting amount 151.19, fry 2016, qys 30" in {
      MockForecastingService.forecastPost2016StatePension(2016, 151.19, 30) shouldBe 155.64
    }

    "return 155.64 for starting amount 146.75, fry 2017, qys 30" in {
      MockForecastingService.forecastPost2016StatePension(2017, 146.75, 30) shouldBe 155.64
    }
  }

  "Forecast Contracted Out" should {
    "return 154.87 for 10000 last year earnings, 2013/14 last year, 35QYs, 2015 FRY" in {
      MockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2015,
        contractedOutLastYear = true
      ) shouldBe 154.87
    }

    "return 155.65 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY" in {
      MockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 155.65
    }

    "return 222.06 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      MockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 100,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 222.06
    }

    "return 145.98 for 10000 last year earnings, 2013/14 last year, 30QYs, 2016 FRY" in {
      MockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 30,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 145.98
    }
  }

  def npsAmountA2016(ap: BigDecimal = 0.0): NpsAmountA2016 = NpsAmountA2016(119.30, Some(ap), None, None, None, None, None, None, 0.00)
  def npsAmountB2016: NpsAmountB2016 = NpsAmountB2016(Some(151.25), None)
  "getForecastAmount" should {

    val testNino = TestAccountBuilder.regularNino
    val hc = HeaderCarrier()

    "not return forecast amount 1234 with scheme membership that has ended" in {
      MockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2010,3,5)), Some(NpsDate(2011,3,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2012-04-04, earnings included up to 2013-04-05" in {
      MockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,4)), Some(NpsDate(2012,4,4)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-05, earnings included up to 2013-04-05" in {
      MockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,3,5)), Some(NpsDate(2013,4,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-06, earnings included up to 2013-04-05" in {
      MockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,5)), Some(NpsDate(2013,4,6)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "return 53.31 for scheme membership in last tax year (not at end), 8 QYs" in {
      MockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,5,5)), Some(NpsDate(2013,8,8)))
      ), NpsDate(2014,4,5), 8, npsAmountA2016(10), npsAmountB2016, 0, 2015, 0, 0, lastYearQualifying = true, testNino)(hc) shouldBe SPAmountModel(53.31)
    }

    "return 222.06 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      MockForecastingService.getForecastAmount(
        List(NpsSchemeMembership(Some(NpsDate(2012,5,5)),Some(NpsDate(2014,8,8)))),
        NpsDate(2014,4,5), 35, npsAmountA2016(100), npsAmountB2016, 10000, 2016, 0, 0, lastYearQualifying = true, testNino
      )(hc) shouldBe SPAmountModel(222.06)
    }
  }

  "adjustForecast" should {
    "return 155.65 for forecast 2016 of 153.88" in {
      MockForecastingService.adjustForecast(0, 153.88, 0, 0) shouldBe 155.65
    }

    "not return 155.64 for forecast 2016 of 153.87" in {
      MockForecastingService.adjustForecast(0, 153.87, 0, 0) should not be 155.64
    }

    "return 1.77 for forecast 2016 of 153.87 and amountB 1.77" in {
      MockForecastingService.adjustForecast(0, 153.87, 0.01, 1.77) shouldBe 1.77
    }

    "return 10 for forecast amount 10, amountA 0.99, amountB 2.77" in {
      MockForecastingService.adjustForecast(10, 0, 0.99, 2.77) shouldBe 10
    }

    "return 11.77 for forecast amount 10, amountA 1, amountB 2.77" in {
      MockForecastingService.adjustForecast(10, 0, 1, 2.77) shouldBe 11.77
    }

    "return 155.65 for forecast amount 154.65, amountA 154.65, amountB 120" in {
      MockForecastingService.adjustForecast(154.65, 0, 154.65, 120) shouldBe 155.65
    }
  }
}
