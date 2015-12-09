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

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.nisp.utils.NISPConstants

case class NpsDate (localDate: LocalDate = new LocalDate()) {
  val toNpsString = NpsDate.dateFormat.print(localDate)

  val taxYear: Int = {
    val year = localDate.year.get
    if (localDate.isBefore(new LocalDate(year, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearStartDay))) year - 1 else year
  }

  lazy val taxYearEndDate: NpsDate = NpsDate(taxYear + 1, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearEndDay)

  val lastProcessedTaxYear: Int = taxYear - 2

  def plusDays(days: Int): NpsDate = NpsDate(localDate.plusDays(days))

  def plusYears(years: Int): NpsDate = NpsDate(localDate.plusYears(years))

  def taxYearsUntil(endDate: NpsDate): Range = this.taxYear to endDate.taxYear

  def classTwoTaxYearsUntil(endDate: NpsDate): Range = taxYearsUntil(NpsDate(endDate.localDate.minusDays(endDate.localDate.dayOfWeek().get()
    .min(NISPConstants.saturday))))

  def max(other: NpsDate): NpsDate = if (localDate.compareTo(other.localDate) < 0) other else this

  def min(other: NpsDate): NpsDate = if (localDate.compareTo(other.localDate) > 0) other else this

  def roundUpToDay(dayOfWeek: Int): NpsDate = {
    if(localDate.dayOfWeek().get() > dayOfWeek) {
      NpsDate(localDate.plusWeeks(1).withDayOfWeek(dayOfWeek))
    } else {
      NpsDate(localDate.withDayOfWeek(dayOfWeek))
    }
  }
}

object NpsDate {
  private val dateFormat = DateTimeFormat.forPattern("dd/MM/yyyy")
  private val npsDateRegex = """^(\d\d)/(\d\d)/(\d\d\d\d)$""".r
  private val npsDateRegex2 = """^(\d\d\d\d)-(\d\d)-(\d\d)$""".r

  implicit val reads = new Reads[NpsDate] {
    override def reads(json:JsValue): JsResult[NpsDate] = {
      json match {
        case JsString(npsDateRegex(d,m,y)) => JsSuccess(NpsDate(new LocalDate(y.toInt, m.toInt, d.toInt)))
        case JsString(npsDateRegex2(y,m,d)) => JsSuccess(NpsDate(new LocalDate(y.toInt, m.toInt, d.toInt)))
        case JsNull => JsError(ValidationError("Null date cannot convert to NpsDate"))
      }
    }
  }

  implicit val writes = new Writes[NpsDate] {
    override def writes(date: NpsDate): JsValue = JsString(date.toNpsString)
  }

  implicit def ordering[A <: NpsDate]: Ordering[NpsDate] = new Ordering[NpsDate] {
    override def compare(x: NpsDate, y: NpsDate): Int = {
      x.localDate.compareTo(y.localDate)
    }
  }

  def startOfTaxYear(taxYear: Int): NpsDate = NpsDate(taxYear, NISPConstants.taxYearStartEndMonth, NISPConstants.taxYearStartDay)
  def apply(year: Int, month: Int, day: Int): NpsDate = NpsDate(new LocalDate(year, month, day))
}
