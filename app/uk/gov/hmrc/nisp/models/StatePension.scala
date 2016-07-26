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

package uk.gov.hmrc.nisp.models

import play.api.libs.json.{Format, JsPath, Json, Writes}
import play.api.libs.functional.syntax._
import org.joda.time.LocalDate
import uk.gov.hmrc.nisp.models.enums.Exclusion.Exclusion

import scala.math.BigDecimal.RoundingMode

case class StatePensionAmount(yearsToWork: Option[Int],
                              gapsToFill: Option[Int],
                              weeklyAmount: BigDecimal) {

  val monthlyAmount: BigDecimal = (((weeklyAmount / 7) * 365.25) / 12).setScale(2, RoundingMode.HALF_UP)
  val annualAmount: BigDecimal = ((weeklyAmount / 7) * 365.25).setScale(2, RoundingMode.HALF_UP)
}
object StatePensionAmount {

  implicit val reads = Json.reads[StatePensionAmount]
  implicit val writes: Writes[StatePensionAmount] = (
    (JsPath \ "yearsToWork").writeNullable[Int] and
    (JsPath \ "gapsToFill").writeNullable[Int] and
    (JsPath \ "weeklyAmount").write[BigDecimal] and
    (JsPath \ "monthlyAmount").write[BigDecimal] and
    (JsPath \ "annualAmount").write[BigDecimal]
    )((sp: StatePensionAmount) => (sp.yearsToWork, sp.gapsToFill, sp.weeklyAmount, sp.monthlyAmount, sp.annualAmount))

  implicit val formats: Format[StatePensionAmount] = Format(reads, writes)

}


case class StatePensionAmounts(protectedPayment: Boolean,
                               current: StatePensionAmount,
                               forecast: StatePensionAmount,
                               maximum: StatePensionAmount,
                               cope: StatePensionAmount)

object StatePensionAmounts {
  implicit val formats = Json.format[StatePensionAmounts]
}


case class StatePension(earningsIncludedUpTo: LocalDate,
                        amounts: StatePensionAmounts,
                        pensionAge: Int,
                        pensionDate: LocalDate,
                        finalRelevantYear: Int,
                        numberOfQualifyingYears: Int,
                        pensionSharingOrder: Boolean,
                        currentFullWeeklyPensionAmount: BigDecimal)

object StatePension {
  implicit val formats = Json.format[StatePension]
}


case class StatePensionExclusion(exclusionReasons: List[Exclusion],
                                 pensionAge: Int,
                                 pensionDate: LocalDate)

object StatePensionExclusion {
  implicit val formats = Json.format[StatePensionExclusion]
}

