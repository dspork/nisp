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

package uk.gov.hmrc.nisp.models.nps

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class NpsNITaxYear(taxYear:Int,
                        qualifyingInt: Int,
                        underInvestigationInt: Int,
                        payable: Int,
                        amountNeeded: Option[BigDecimal],
                        classThreePayable: Option[BigDecimal],
                        classThreePayableBy: Option[NpsDate],
                        classThreePayableByPenalty: Option[NpsDate],
                        classTwoPayable: Option[BigDecimal],
                        classTwoPayableBy: Option[NpsDate],
                        classTwoPayableByPenalty: Option[NpsDate],
                        classTwoOutstandingWeeks: Option[Int],
                        niEarningsEmployedOption: Option[String],
                        niEarningsOption: Option[BigDecimal],
                        contractedOutClassOnePaidOption: Option[BigDecimal],
                        contractedOutPrimaryPaidEarningsOption: Option[BigDecimal],
                        selfEmployedCreditsOption: Option[String],
                        voluntaryCreditsOption: Option[String],
                        primaryPaidEarningsOption: Option[String],
                        otherCreditsOption: Option[List[NpsOtherCredits]]) {

  val qualifying = qualifyingInt == 1
  val underInvestigation: Boolean = underInvestigationInt == 1
  val niEarningsEmployed: BigDecimal = niEarningsEmployedOption.map(BigDecimal(_)) getOrElse 0
  val niEarnings: BigDecimal = niEarningsOption.getOrElse(0)
  val contractedOutClassOnePaid: BigDecimal = contractedOutClassOnePaidOption.getOrElse(0)
  val contractedOutPrimaryPaidEarnings: BigDecimal = contractedOutPrimaryPaidEarningsOption.getOrElse(0)
  val selfEmployedCredits: Int = selfEmployedCreditsOption.getOrElse("0").toInt
  val voluntaryCredits: Int = voluntaryCreditsOption.getOrElse("0").toInt
  val otherCredits: List[NpsOtherCredits] = otherCreditsOption.getOrElse(List())
  val primaryPaidEarnings: BigDecimal = primaryPaidEarningsOption.map(BigDecimal(_)) getOrElse 0
}

object NpsNITaxYear {
  implicit val formats: Format[NpsNITaxYear] = (
    (__ \ "rattd_tax_year").format[Int] and
      (__ \ "qualifying").format[Int] and
      (__ \ "under_investigation_flag").format[Int] and
      (__ \ "payable").format[Int] and
      (__ \ "amount_needed").format[Option[BigDecimal]] and
      (__ \ "class_three_payable").format[Option[BigDecimal]] and
      (__ \ "class_three_payable_by").format[Option[NpsDate]] and
      (__ \ "class_three_payable_by_penalty").format[Option[NpsDate]] and
      (__ \ "class_two_payable").format[Option[BigDecimal]] and
      (__ \ "class_two_payable_by").format[Option[NpsDate]] and
      (__ \ "class_two_payable_by_penalty").format[Option[NpsDate]] and
      (__ \ "class_two_outstanding_weeks").format[Option[Int]] and
      (__ \ "ni_earnings_employed").format[Option[String]]  and
      (__ \ "ni_earnings").format[Option[BigDecimal]] and
      (__ \ "co_class_one_paid").format[Option[BigDecimal]] and
      (__ \ "co_primary_paid_earnings").format[Option[BigDecimal]] and
      (__ \ "ni_earnings_self_employed").format[Option[String]] and
      (__ \ "ni_earnings_voluntary").format[Option[String]] and
      (__ \ "primary_paid_earnings").format[Option[String]] and
      (__ \ "npsLothcred").format[Option[List[NpsOtherCredits]]]
    )(NpsNITaxYear.apply, unlift(NpsNITaxYear.unapply))
}
