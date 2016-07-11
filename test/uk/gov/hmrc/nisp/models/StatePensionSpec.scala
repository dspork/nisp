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

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json._
import uk.gov.hmrc.nisp.models
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.test.UnitSpec

class StatePensionSpec extends UnitSpec {

  "StatePensionAmount" should {
    "Weekly / Monthly / Annual Calculation" should {
      "return 151.25, 657.67, 7892.01" in {
        StatePensionAmount(None, None, 151.25).monthlyAmount shouldBe 657.67
        StatePensionAmount(None, None, 151.25).annualAmount shouldBe 7892.01
      }

      "return 43.21, 187.89, 2254.64" in {
        StatePensionAmount(None, None, 43.21).monthlyAmount shouldBe 187.89
        StatePensionAmount(None, None, 43.21).annualAmount shouldBe 2254.64
      }

      "return 95.07, 413.38, 4960.62" in {
        StatePensionAmount(None, None, 95.07).monthlyAmount shouldBe 413.38
        StatePensionAmount(None, None, 95.07).annualAmount shouldBe 4960.62
      }

      "yearsToWork and gapsToFill have no bearing on calculation" in {
        StatePensionAmount(Some(2), None, 95.07).monthlyAmount shouldBe 413.38
        StatePensionAmount(None, Some(2), 95.07).annualAmount shouldBe 4960.62
        StatePensionAmount(None, Some(2), 95.07).monthlyAmount shouldBe 413.38
        StatePensionAmount(Some(2), None, 95.07).annualAmount shouldBe 4960.62
        StatePensionAmount(Some(2), Some(2), 95.07).monthlyAmount shouldBe 413.38
        StatePensionAmount(Some(2), Some(2), 95.07).annualAmount shouldBe 4960.62
      }
    }
  }

  "StatePension" should {
    "format to JSON properly" in {
      val statePension = StatePension(
        new LocalDate(2015, 4, 5),
        StatePensionAmounts(
          protectedPayment = false,
          StatePensionAmount(None, None, 123.65),
          StatePensionAmount(Some(4), None, 151.25),
          StatePensionAmount(Some(4), Some(1), 155.65),
          StatePensionAmount(None, None, 0.25)
        ),
        67,
        new LocalDate(2019, 7, 1),
        2018,
        30,
        pensionSharingOrder = false,
        155.65
      )

      val json = Json.toJson(statePension)
      (json \ "earningsIncludedUpTo").as[NpsDate] shouldBe NpsDate(2015, 4, 5)
      (json \ "amounts" \ "protectedPayment").as[Boolean] shouldBe false
      (json \ "amounts" \ "maximum" \ "yearsToWork").as[Int] shouldBe 4
      (json \ "amounts" \ "maximum" \ "gapsToFill").as[Int] shouldBe 1
      (json \ "amounts" \ "maximum" \ "weeklyAmount").as[BigDecimal] shouldBe 155.65
      (json \ "amounts" \ "maximum" \ "monthlyAmount").as[BigDecimal] shouldBe 676.80
      (json \ "amounts" \ "maximum" \ "annualAmount").as[BigDecimal] shouldBe 8121.59
      (json \ "pensionAge").as[Int] shouldBe 67
      (json \ "pensionDate").as[NpsDate] shouldBe NpsDate(2019, 7, 1)
      (json \ "finalRelevantYear").as[Int] shouldBe 2018
      (json \ "numberOfQualifyingYears").as[Int] shouldBe 30
      (json \ "pensionSharingOrder").as[Boolean] shouldBe false
      (json \ "currentWeeklyPensionAmount").as[BigDecimal] shouldBe 155.65
    }

    "parse from JSON" in {
      val statePension = Json.fromJson[StatePension](
        Json.parse(
          """
                     |{
                     |  "earningsIncludedUpTo": "2015-04-05",
                     |  "amounts": {
                     |    "protectedPayment": false,
                     |    "current": {
                     |      "weeklyAmount": 123.65
                     |    },
                     |    "forecast": {
                     |      "yearsToWork": 4,
                     |      "weeklyAmount": 151.25
                     |    },
                     |    "maximum": {
                     |      "yearsToWork": 4,
                     |      "gapsToFill": 1,
                     |      "weeklyAmount": 155.65
                     |    },
                     |    "cope": {
                     |      "weeklyAmount": 0.25
                     |    }
                     |  },
                     |  "pensionAge": 67,
                     |  "pensionDate": "2019-07-01",
                     |  "finalRelevantYear": 2018,
                     |  "numberOfQualifyingYears": 30,
                     |  "pensionSharingOrder": false,
                     |  "currentWeeklyPensionAmount": 155.65
                     |  }
                    """.stripMargin
         )
       )

      statePension.map(_ shouldBe StatePension(
        new LocalDate(2015, 4, 5),
        StatePensionAmounts(
          protectedPayment = false,
          StatePensionAmount(None, None, 123.65),
          StatePensionAmount(Some(4), None, 151.25),
          StatePensionAmount(Some(4), Some(1), 155.65),
          StatePensionAmount(None, None, 0.25)
        ),
        67,
        new LocalDate(2019, 7 ,1),
        2018,
        30,
        pensionSharingOrder = false,
        155.65
      )).getOrElse(throw new Exception("could not parse json"))
    }
  }
}
