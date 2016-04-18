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

package uk.gov.hmrc.nisp.models.nps

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class NpsNIRecordModel( nino:String,
                            numberOfQualifyingYears:Int,
                            nonQualifyingYears: Int,
                            yearsToFry: Int,
                            nonQualifyingYearsPayable: Int,
                            pre75ContributionCount: Int,
                            dateOfEntry: NpsDate,
                            niTaxYears: List[NpsNITaxYear]) {
  def purge(fry: Int) = {
    val taxYears = niTaxYears.filter(_.taxYear <= fry)
    this.copy(nonQualifyingYears = taxYears.count(!_.qualifying), nonQualifyingYearsPayable = taxYears.count(year => !year.qualifying && year.payable), niTaxYears = taxYears)
  }
}

object NpsNIRecordModel {
  implicit val formats: Format[NpsNIRecordModel] = (
      (__ \ "nino").format[String] and
      (__ \ "number_of_qualifying_years").format[Int] and
      (__ \ "non_qualifying_years").format[Int] and
      (__ \ "years_to_fry").format[Int] and
      (__ \ "non_qualifying_years_payable").format[Int] and
      (__ \ "pre_75_cc_count").format[Int] and
      (__ \ "date_of_entry").format[NpsDate] and
      (__ \ "npsLnitaxyr").format[List[NpsNITaxYear]]
    )(NpsNIRecordModel.apply, unlift(NpsNIRecordModel.unapply))
}
