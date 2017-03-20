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

case class NpsSchemeMembership(startDate: NpsDate, endDate: Option[NpsDate], sequenceNumber: Int, occurrenceNumber: Int) {
  def contains(npsDate: NpsDate): Boolean = {
    (startDate, endDate) match {
      case (start, None) => npsDate.localDate.isAfter(start.localDate) || npsDate.localDate.isEqual(start.localDate)
      case (start, Some(end)) =>
        (npsDate.localDate.isBefore(end.localDate) || npsDate.localDate.isEqual(end.localDate)) &&
        (npsDate.localDate.isAfter(start.localDate) || npsDate.localDate.isEqual(start.localDate))
    }
  }

  def existsInTaxYear(taxYear: Int): Boolean = {
    (startDate, endDate) match {
      case (start, None) => start.taxYear <= taxYear
      case (start, Some(end)) => start.taxYear <= taxYear && end.taxYear >= taxYear
    }
  }
}

object NpsSchemeMembership {
  implicit val formats: Format[NpsSchemeMembership] = (
    (__ \ "scheme_mem_start_date").format[NpsDate] and
    (__ \ "scheme_end_date").formatNullable[NpsDate] and
    (__ \ "scheme_membership_seq_no").format[Int] and
    (__ \ "scheme_memb_occ_no").format[Int]
    )(NpsSchemeMembership.apply, unlift(NpsSchemeMembership.unapply))
}
