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

package uk.gov.hmrc.nisp.models

import play.api.libs.json._
import uk.gov.hmrc.nisp.services.reference.QualifyingYearsAmountService
import uk.gov.hmrc.nisp.utils.NISPConstants

import scala.math.BigDecimal.RoundingMode

case class SPAmountModel(week: BigDecimal) {
  val month = (((week / 7) * 365.25) / 12).setScale(2, RoundingMode.HALF_UP)
  val year = ((week / 7) * 365.25).setScale(2, RoundingMode.HALF_UP)
}

object SPAmountModel {
  implicit val spAmountModelReads = new Reads[SPAmountModel] {
    def reads(js: JsValue): JsResult[SPAmountModel] = {
      JsSuccess(SPAmountModel(
        (js \ "week").as[BigDecimal]
      ))
    }
  }

  implicit val spAmountModelWrites: Writes[SPAmountModel] = new Writes[SPAmountModel] {
    def writes(sPAmountModel: SPAmountModel): JsValue = {
      Json.obj(
        "week" -> sPAmountModel.week,
        "month" -> sPAmountModel.month,
        "year" -> sPAmountModel.year
      )
    }

    implicit val formats = Format(spAmountModelReads, spAmountModelWrites)
  }

  def getWeeklyAmount(numberOfQualifyingYears: Int, additionalPension: BigDecimal): SPAmountModel = {
    if(numberOfQualifyingYears < NISPConstants.newStatePensionMinimumQualifyingYears) {
      SPAmountModel(0)
    } else {
      getWeeklyAmountRaw(numberOfQualifyingYears, additionalPension)
    }
  }

  def getWeeklyAmountRaw(numberOfQualifyingYears: Int, additionalPension: BigDecimal): SPAmountModel = {
    SPAmountModel(getAmountA(numberOfQualifyingYears, additionalPension).max(getAmountB(numberOfQualifyingYears)))
  }

  def getAmountA(numberOfQualifyingYears: Int, additionalPension: BigDecimal): BigDecimal =
    QualifyingYearsAmountService.getBspAmount(numberOfQualifyingYears) + additionalPension

  def getAmountB(numberOfQualifyingYears: Int): BigDecimal = QualifyingYearsAmountService.getNspAmount(numberOfQualifyingYears)
}
