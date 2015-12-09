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

package uk.gov.hmrc.nisp.services

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.CustomAuditConnector
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockCustomAuditConnector}
import uk.gov.hmrc.nisp.models.SPAmountModel
import uk.gov.hmrc.nisp.models.nps.{NpsSchemeMembership, NpsAmountB2016, NpsAmountA2016, NpsDate}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class ForecastingServiceSpec extends UnitSpec with OneAppPerSuite {

  val mockForecastingService = new ForecastingService {
    override val customAuditConnector: CustomAuditConnector = MockCustomAuditConnector
  }
  
  "QYs to 2016" should {
    "return 2 for earnings included up to of 2014-04-05" in {
      mockForecastingService.qualifyingYearsTo2016(NpsDate(2014,4,5)) shouldBe 2
    }

    "return 1 for earnings included up to of 2015-04-05" in {
      mockForecastingService.qualifyingYearsTo2016(NpsDate(2015,4,5)) shouldBe 1
    }

    "return 0 for earnings included up to of 2016-04-05" in {
      mockForecastingService.qualifyingYearsTo2016(NpsDate(2016,4,5)) shouldBe 0
    }

    "return 0 for earnings included up to of 2017-04-05" in {
      mockForecastingService.qualifyingYearsTo2016(NpsDate(2017,4,5)) shouldBe 0
    }
  }

  "QYs at 2016" should {
    "return 32 for earnings included up to of 2014-04-05 and current QYs 30" in {
      mockForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 30) shouldBe 32
    }

    "return 2 for earnings included up to of 2014-04-05 and current QYs 0" in {
      mockForecastingService.totalQualifyingYearsAt2016(NpsDate(2014,4,5), 0) shouldBe 2
    }
  }

  "AP to accrue for 2016 forecast" should {
    "return 0 for earnings included up to 2017-04-05 and ap_amount 1" in {
      mockForecastingService.forecastAPToAccrue(NpsDate(2017,4,5), 1) shouldBe 0
    }

    "return 0 for earnings included up to 2016-04-05 and ap_amount 1" in {
      mockForecastingService.forecastAPToAccrue(NpsDate(2016,4,5), 1) shouldBe 0
    }

    "return 1 for earnings included up to 2015-04-05 and ap_amount 1" in {
      mockForecastingService.forecastAPToAccrue(NpsDate(2015,4,5), 1) shouldBe 1
    }

    "return 2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      mockForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1) shouldBe 2
    }

    "return 3.2 for earnings included up to 2014-04-05 and ap_amount 1" in {
      mockForecastingService.forecastAPToAccrue(NpsDate(2014,4,5), 1.6) shouldBe 3.2
    }
  }

  "Amount A at 2016" should {
    "return 0 for BP of 0, existing AP 0, forecasted AP 0" in {
      mockForecastingService.forecastAmountAAt2016(0, 0, 0) shouldBe 0
    }

    "return 115.95 for BP of 115.95, existing AP 0, forecasted AP 0" in {
      mockForecastingService.forecastAmountAAt2016(115.95, 0, 0) shouldBe 115.95
    }

    "return 125.95 for BP of 115.95, existing AP 10, forecasted AP 0" in {
      mockForecastingService.forecastAmountAAt2016(115.95, 10, 0) shouldBe 125.95
    }

    "return 127.75 for BP of 115.95, existing AP 10, forecasted AP 1.8" in {
      mockForecastingService.forecastAmountAAt2016(115.95, 10, 1.8) shouldBe 127.75
    }
  }

  "UAP capped earnings" should {
    "return 0 for 0 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(0) shouldBe 0
    }

    "return 40039 for 40039 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(40039) shouldBe 40039
    }

    "return 40040 for 40040 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(40040) shouldBe 40040
    }

    "return 40040 for 40041 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(40041) shouldBe 40040
    }

    "return 40040 for 40040.6 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(40040.6) shouldBe 40040
    }

    "return 34571.54 for 34571.54 earnings" in {
      mockForecastingService.cappedEarningsAtUAP(34571.54) shouldBe 34571.54
    }
  }

  "Band 2" should {
    "return 0 for 0 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 14999 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(14999, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 15000 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(15000, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 15001 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(15001, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 1.5 for 15001.5 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(15001.5, NpsDate(2014,4,5)) shouldBe 1.5
    }

    "return 15001.5 for 30001.5 earnings 2014-04-05 earnings included up to" in {
      mockForecastingService.band2(30001.5, NpsDate(2014,4,5)) shouldBe 15001.5
    }
  }

  "S2P Amount" should {
    "return FRAA (1.77) for band 2 £0" in {
      mockForecastingService.s2pAmount(NpsDate(2013,4,5), 0) shouldBe 1.77
    }
    
    "return FRAA (1.77) for band 2 £109.40" in {
      mockForecastingService.s2pAmount(NpsDate(2013,4,5), 109.40) shouldBe 1.77
    }

    "return FRAA + 0.01 (1.78) for band 2 £109.41" in {
      mockForecastingService.s2pAmount(NpsDate(2013,4,5), 109.41) shouldBe 1.78
    }

    "return FRAA + 0.11 (1.88) for band 2 £2501" in {
      mockForecastingService.s2pAmount(NpsDate(2013,4,5), 2501) shouldBe 1.88
    }

    "return 2.89 for 40040 earnings (band 2 25040)" in {
      mockForecastingService.s2pAmount(NpsDate(2014,4,5), 25040) shouldBe 2.89
    }
  }

  "Earnings above LEL" should {
    "return 0 for 0 earnings" in {
      mockForecastingService.earningsAboveLEL(0, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 0 for 5668 earnings" in {
      mockForecastingService.earningsAboveLEL(5668, NpsDate(2014,4,5)) shouldBe 0
    }

    "return 1 for 5669 earnings" in {
      mockForecastingService.earningsAboveLEL(5669, NpsDate(2014,4,5)) shouldBe 1
    }

    "return 0.01 for 5668.01 earnings" in {
      mockForecastingService.earningsAboveLEL(5668.01, NpsDate(2014,4,5)) shouldBe 0.01
    }
  }

  "S2P Deduction" should {
    "return 0 for 0 total earnings" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5),0,0,0) shouldBe 0
    }

    "return 1.4 for 40041 total earnings and 1.4 S2P Amount" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40041, 1.4) shouldBe 1.4
    }

    "return 0 for 40040 total earnings and 1.4 S2P Amount" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 0, 40040, 1.4) shouldBe 0
    }

    "return 1.4 for 40040.01 total earnings and 1.4 S2P Amount" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 35000, 40040.01, 1.4) shouldBe 1.4
    }

    "return 0 for 54.70 earnings above LEL" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.70, 35000, 1.4) shouldBe 0
    }

    "return 0.01 for 54.71 earnings above LEL" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 54.71, 35000, 1.4) shouldBe 0.01
    }

    "return 1.83 for 20000 earnings above LEL" in {
      mockForecastingService.s2pDeduction(NpsDate(2013,4,5), 20000, 35000, 1.4) shouldBe 1.83
    }

    "return 0.73 for 8162 earnings above LEL for 2013/14" in {
      mockForecastingService.s2pDeduction(NpsDate(2014,4,5), 8162, 35000, 1.4) shouldBe 0.73
    }
  }

  "Qualifying Years to FRY" should {
    "return 0 for FRY 2015" in {
      mockForecastingService.qualifyingYearsToFRY(2015) shouldBe 0
    }

    "return 1 for FRY 2016" in {
      mockForecastingService.qualifyingYearsToFRY(2016) shouldBe 1
    }

    "return 2 for FRY 2017" in {
      mockForecastingService.qualifyingYearsToFRY(2017) shouldBe 2
    }
  }

  "Forecaste RDA at 2016" should {
    "return 0 for 0 existing RDA and 0 accrued RDA" in {
      mockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 0) shouldBe 0
    }

    "return 10 for 10 existing RDA and 0 accrued RDA" in {
      mockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 0, 10) shouldBe 10
    }

    "return 10 for 0 existing RDA and 10 accrued RDA, earnings included up to 2014/15" in {
      mockForecastingService.forecastRDAAt2016(NpsDate(2015,4,5), 10, 0) shouldBe 10
    }

    "return 20 for 0 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      mockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 0) shouldBe 20
    }

    "return 25 for 5 existing RDA and 10 accrued RDA, earnings included up to 2013/14" in {
      mockForecastingService.forecastRDAAt2016(NpsDate(2014,4,5), 10, 5) shouldBe 25
    }
  }

  "Forecast Amount B at 2016" should {
    "return 3 for main amount 4, rebate derived amount 1" in {
      mockForecastingService.forecastAmountBAt2016(4,1)
    }
  }

  "Forecast Post 2016 New State Pension" should {
    "return 151.26 for starting amount 151.26, fry 2018, qys 30" in {
      mockForecastingService.forecastPost2016StatePension(2018, 151.26, 30) shouldBe 151.26
    }

    "return 151.26 for starting amount 151.24, fry 2015, qys 10" in {
      mockForecastingService.forecastPost2016StatePension(2015, 151.24, 10) shouldBe 151.24
    }

    "return 0 for starting amount 151.24, fry 2015, qys 9" in {
      mockForecastingService.forecastPost2016StatePension(2015, 151.24, 9) shouldBe 0
    }

    "return non-zero for starting amount 151.24, fry 2016, qys 9" in {
      mockForecastingService.forecastPost2016StatePension(2016, 151.24, 9) should not be 0
    }

    "return 151.25 for starting amount 151.24, fry 2016, qys 30" in {
      mockForecastingService.forecastPost2016StatePension(2016, 151.24, 30) shouldBe 151.25
    }

    "return 151.24 for starting amount 146.92, fry 2016, qys 30" in {
      mockForecastingService.forecastPost2016StatePension(2016, 146.92, 30) shouldBe 151.24
    }

    "return 151.24 for starting amount 142.60, fry 2017, qys 30" in {
      mockForecastingService.forecastPost2016StatePension(2017, 142.60, 30) shouldBe 151.24
    }
  }

  "Forecast Contracted Out" should {
    "return 150.47 for 10000 last year earnings, 2013/14 last year, 35QYs, 2015 FRY" in {
      mockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2015,
        contractedOutLastYear = true
      ) shouldBe 150.47
    }

    "return 151.25 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY" in {
      mockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 151.25
    }

    "return 218.71 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      mockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 35,
        existingRDA = 0,
        existingAP = 100,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 218.71
    }

    "return 141.83 for 10000 last year earnings, 2013/14 last year, 30QYs, 2016 FRY" in {
      mockForecastingService.forecast(
        earningsIncludedUpTo = NpsDate(2014,4,5),
        currentQualifyingYears = 30,
        existingRDA = 0,
        existingAP = 0,
        lastYearEarnings = 10000,
        finalRelevantYear = 2016,
        contractedOutLastYear = true
      ) shouldBe 141.83
    }
  }

  def npsAmountA2016(ap: BigDecimal = 0.0): NpsAmountA2016 = NpsAmountA2016(115.95, Some(ap), None, None, None, None, None, None, 0.00)
  def npsAmountB2016: NpsAmountB2016 = NpsAmountB2016(Some(151.25), None)
  "getForecastAmount" should {

    val testNino = TestAccountBuilder.regularNino
    val hc = HeaderCarrier()

    "return forecast amount 1234 with no scheme membership" in {
      mockForecastingService.getForecastAmount(
        List(), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino
      )(hc) shouldBe SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that has ended" in {
      mockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2010,3,5)), Some(NpsDate(2011,3,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2012-04-04, earnings included up to 2013-04-05" in {
      mockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,4)), Some(NpsDate(2012,4,4)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-05, earnings included up to 2013-04-05" in {
      mockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,3,5)), Some(NpsDate(2013,4,5)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "not return forecast amount 1234 with scheme membership that ends on 2013-04-06, earnings included up to 2013-04-05" in {
      mockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2012,3,5)), Some(NpsDate(2013,4,6)))
      ), NpsDate(2013,4,5), 0, npsAmountA2016(), npsAmountB2016, 0, 0, 1234, 0, lastYearQualifying = true, testNino)(hc) should not be SPAmountModel(1234)
    }

    "return 52.19 for scheme membership in last tax year (not at end), 8 QYs" in {
      mockForecastingService.getForecastAmount(List(
        NpsSchemeMembership(Some(NpsDate(2013,5,5)), Some(NpsDate(2013,8,8)))
      ), NpsDate(2014,4,5), 8, npsAmountA2016(10), npsAmountB2016, 0, 2015, 0, 0, lastYearQualifying = true, testNino)(hc) shouldBe SPAmountModel(52.19)
    }

    "return 218.71 for 10000 last year earnings, 2013/14 last year, 35QYs, 2016 FRY, existing AP 100" in {
      mockForecastingService.getForecastAmount(
        List(NpsSchemeMembership(Some(NpsDate(2012,5,5)),Some(NpsDate(2014,8,8)))),
        NpsDate(2014,4,5), 35, npsAmountA2016(100), npsAmountB2016, 10000, 2016, 0, 0, lastYearQualifying = true, testNino
      )(hc) shouldBe SPAmountModel(218.71)
    }
  }

  "adjustForecast" should {
    "return 151.25 for forecast 2016 of 149.48" in {
      mockForecastingService.adjustForecast(0, 149.48, 0, 0) shouldBe 151.25
    }

    "not return 151.24 for forecast 2016 of 149.47" in {
      mockForecastingService.adjustForecast(0, 149.47, 0, 0) should not be 151.24
    }

    "return 1.77 for forecast 2016 of 149.47 and amountB 1.77" in {
      mockForecastingService.adjustForecast(0, 149.47, 0.01, 1.77) shouldBe 1.77
    }

    "return 10 for forecast amount 10, amountA 0.99, amountB 2.77" in {
      mockForecastingService.adjustForecast(10, 0, 0.99, 2.77) shouldBe 10
    }

    "return 11.77 for forecast amount 10, amountA 1, amountB 2.77" in {
      mockForecastingService.adjustForecast(10, 0, 1, 2.77) shouldBe 11.77
    }

    "return 151.25 for forecast amount 150.25, amountA 150.25, amountB 120" in {
      mockForecastingService.adjustForecast(150.25, 0, 150.25, 120) shouldBe 151.25
    }
  }
}
