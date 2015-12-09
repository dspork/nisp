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

import org.joda.time.{Days, LocalDate, Weeks, Period}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.nisp.utils.NISPConstants

case class NpsLiability(liabilityType: Int, start: Option[NpsDate], end: Option[NpsDate]) {
  def weeksLiableForTaxYear(taxYear: Int): Int =
    Weeks.weeksBetween(
      start.getOrElse(NpsDate(0,0,0)).max(NpsDate(taxYear, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearStartDay)
        .roundUpToDay(NISPConstants.sunday)).localDate,
      end.getOrElse(NpsDate(new LocalDate())).min(NpsDate(taxYear + 1, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearEndDay)
        .roundUpToDay(NISPConstants.saturday)).localDate.plusDays(1)
    ).getWeeks.max(0)

  def daysLiableForTaxYear(taxYear: Int): Int =
    Days.daysBetween(
      start.getOrElse(NpsDate(0,0,0)).max(NpsDate(taxYear, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearStartDay)).localDate,
      end.getOrElse(NpsDate()).min(NpsDate(taxYear + 1, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearEndDay)).localDate.plusDays(1)
    ).getDays.max(0)

  val startDateGetOrElseDefault: NpsDate = start.getOrElse(NpsDate(NISPConstants.defaultMinYear,NISPConstants.defaultMinMonth,NISPConstants.defaultMinDay))
}

object NpsLiability {
  implicit val formats: Format[NpsLiability] = (
      (__ \ "liability_type").format[Int] and
      (__ \ "liability_type_start_date").format[Option[NpsDate]] and
      (__ \ "liability_type_end_date").format[Option[NpsDate]]
    )(NpsLiability.apply, unlift(NpsLiability.unapply))
}
