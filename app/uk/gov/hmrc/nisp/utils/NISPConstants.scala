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

package uk.gov.hmrc.nisp.utils

import org.joda.time.LocalDate

object NISPConstants {

  // scalastyle:off magic.number

  val isleOfManLiability = 5
  val countryNotSpecified = 0
  val countryGB = 1
  val countryNI = 8
  val countryEngland = 114
  val countryScotland = 115
  val countryWales = 116
  val countryIsleOfMan = 7

  val fullBasicPensionQualifyingYears = 30
  val newStatePensionMinimumQualifyingYears = 10

  val factor78Year = 1978
  val post78Factor = 52
  val pre78Factor = 50

  val newStatePensionStartYear = 2016

  val taxYearStartDay = 6
  val taxYearStartEndMonth = 4
  val taxYearEndDay = 5
  val newStatePensionStart = new LocalDate(newStatePensionStartYear, taxYearStartEndMonth, taxYearStartDay)

  val saturday = 6
  val sunday = 7

  val defaultMinYear = 1912
  val defaultMinMonth = 1
  val defaultMinDay = 1

  val ninoLengthWithoutSuffix = 8

  val maxBasicStatePensionAmount = 119.30
  val maxBasicStatePensionYears = 30

  val upperAccrualPoint = 40040

  val weeksInYear = 52

  val fraa2014 = 1.77
  val fraa2015 = 1.8

  val s2pBand2Divisor1 = 10
  val s2pBand2Divisor2 = 44
  val s2pBand2Divisor1Deduction = 5

  val niRecordStart = 1975
  val niRecordMinAge = 16
}
