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

package uk.gov.hmrc.nisp.events

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.models.nps.{NpsAmountB2016, NpsAmountA2016, NpsDate}
import uk.gov.hmrc.play.http.HeaderCarrier

object ForecastingEvent {
  def apply(nino: Nino, earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, amountA: NpsAmountA2016, amountB: NpsAmountB2016,
            lastYearEarnings: BigDecimal, finalRelevantYear: Int, forecastAmount: BigDecimal, forecastAmount2016: BigDecimal, lastYearQualifying: Boolean,
            contractedOutInfo: String)(implicit hc: HeaderCarrier): ForecastingEvent =
    new ForecastingEvent(nino, earningsIncludedUpTo, currentQualifyingYears, amountA, amountB, lastYearEarnings, finalRelevantYear, forecastAmount,
      forecastAmount2016, lastYearQualifying, contractedOutInfo)
}

class ForecastingEvent(nino: Nino, earningsIncludedUpTo: NpsDate, currentQualifyingYears: Int, amountA: NpsAmountA2016,
                       amountB: NpsAmountB2016, lastYearEarnings: BigDecimal, finalRelevantYear: Int,
                       forecastAmount: BigDecimal, forecastAmount2016: BigDecimal, lastYearQualifying: Boolean, contractedOutInfo: String)
                      (implicit hc: HeaderCarrier)
  extends BusinessEvent("Forecasting", Map(
     "nino" -> nino.value,
     "earningsIncludedUpTo" -> earningsIncludedUpTo.toString,
     "currentQualifyingYears" -> currentQualifyingYears.toString,
     "amountAtotal" -> amountA.total.toString(),
     "basicPension" -> amountA.basicPension.toString(),
     "pre97AP" -> amountA.pre97AP.toString(),
     "post97AP" -> amountA.post97AP.toString(),
     "post02AP" -> amountA.post02AP.toString(),
     "pre88GMP" -> amountA.pre88Gmp.toString(),
     "post88GMP" -> amountA.post88Gmp.toString(),
     "pre88COD" -> amountA.pre88Cod.toString(),
     "post88COD" -> amountA.post88Cod.toString(),
     "grb" -> amountA.grb.toString(),
     "totalAP" -> amountA.totalAP.toString(),
     "amountBmain" -> amountB.mainComponent.toString(),
     "rda" -> amountB.rebateDerivedAmount.toString(),
     "lastYearEarnings" -> lastYearEarnings.toString(),
     "fry" -> finalRelevantYear.toString,
     "forecastAmount" -> forecastAmount.toString(),
     "forecastAmount2016" -> forecastAmount2016.toString(),
     "lastYearQualifying" -> lastYearQualifying.toString,
     "contractedOutInfo" -> contractedOutInfo
  ))
