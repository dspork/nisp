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

package uk.gov.hmrc.nisp.models.nps

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.nisp.models.enums.Exclusion
import uk.gov.hmrc.nisp.utils.NISPConstants

case class NpsSummaryModel( nino: String,
                            postcode: Option[String],
                            countryCode: Int,
                            dateOfBirth: NpsDate,
                            dateOfDeath: Option[NpsDate],
                            spaDate: NpsDate,
                            nspQualifyingYears: Int,
                            finalRelevantYear: Int,
                            minimumQualifyingPeriodFlagOption: Option[Int],
                            nspRequisiteYearsOption: Option[Int],
                            rreToConsiderOption: Option[Int],
                            pensionShareOrderCOEGOption: Option[Int],
                            pensionShareOrderSERPSOption: Option[Int],
                            earningsIncludedUpTo: NpsDate,
                            accountNotMaintainedFlag: Option[Int],
                            sensitiveCaseFlag: Int,
                            sex: String,
                            npsStatePensionAmount: NpsStatePensionAmount,
                            pensionForecast: NpsPensionForecast) {
  val pensionShareOrderCOEG = pensionShareOrderCOEGOption.getOrElse(0)
  val pensionShareOrderSERPS = pensionShareOrderSERPSOption.getOrElse(0)
  val minimumQualifyingPeriodFlag = minimumQualifyingPeriodFlagOption.getOrElse(1)
  val nspRequisiteYears = nspRequisiteYearsOption.getOrElse(0)
  val rreToConsider = rreToConsiderOption.getOrElse(0)

  val yearsUntilPensionAge = finalRelevantYear - earningsIncludedUpTo.taxYear

  val isAbroad = countryCode != NISPConstants.countryNotSpecified &&
                  countryCode != NISPConstants.countryGB &&
                  countryCode != NISPConstants.countryNI &&
                  countryCode != NISPConstants.countryEngland &&
                  countryCode != NISPConstants.countryScotland &&
                  countryCode != NISPConstants.countryWales &&
                  countryCode != NISPConstants.countryIsleOfMan

}

object NpsSummaryModel {
  implicit val formats: Format[NpsSummaryModel] = (
      (__ \ "nino").format[String] and
      (__ \ "address_postcode").format[Option[String]] and
      (__ \ "country_code").format[Int] and
      (__ \ "date_of_birth").format[NpsDate] and
      (__ \ "date_of_death").format[Option[NpsDate]] and
      (__ \ "spa_date").format[NpsDate] and
      (__ \ "nsp_qualifying_years").format[Int] and
      (__ \ "final_relevant_year").format[Int] and
      (__ \ "minimum_qualifying_period").format[Option[Int]] and
      (__ \ "nsp_requisite_years").format[Option[Int]] and
      (__ \ "rre_to_consider").format[Option[Int]] and
      (__ \ "pension_share_order_coeg").format[Option[Int]] and
      (__ \ "pension_share_order_serps").format[Option[Int]] and
      (__ \ "earnings_included_upto").format[NpsDate] and
      (__ \ "account_not_maintained_flag").format[Option[Int]] and
      (__ \ "sensitive_flag").format[Int] and
      (__ \ "sex").format[String] and
      (__ \ "npsSpnam").format[NpsStatePensionAmount] and
      (__ \ "npsPenfor").format[NpsPensionForecast]
    )(NpsSummaryModel.apply, unlift(NpsSummaryModel.unapply))
}
