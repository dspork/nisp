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

package uk.gov.hmrc.nisp.services

import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.helpers.{TestAccountBuilder, MockNpsConnector}
import uk.gov.hmrc.nisp.models.NIRecordTaxYear
import uk.gov.hmrc.play.http.{NotFoundException, HeaderCarrier}
import uk.gov.hmrc.play.test.UnitSpec

class NIResponseServiceSpec  extends UnitSpec with MockitoSugar with BeforeAndAfter with OneAppPerSuite {

  val nino = TestAccountBuilder.regularNino
  val nonexistentnino = TestAccountBuilder.nonExistentNino

  implicit val hc = HeaderCarrier()

  val testNIServiceWithMockHttp = new NIResponseService {
    override val nps: NpsConnector = MockNpsConnector
    override def now: LocalDate = new LocalDate()
  }

  "customer with NINO regular has date of entry of 01/04/1972" should {
    "returns NIResponse" in {
      val niResponse = testNIServiceWithMockHttp.getNIResponse(nino)
      niResponse.niRecord.get.taxYears.head shouldBe
        NIRecordTaxYear(1975, qualifying = true, 109.08, 0, 0, 0, None, None, None, payable = false, underInvestigation = false)
      niResponse.niRecord.get.taxYears.last shouldBe
        NIRecordTaxYear(2013, qualifying = true, 0, 52, 0, 0, None, None, None, payable = false, underInvestigation = false)
      niResponse.niRecord.get.taxYears.size shouldBe 39
    }
  }

  "customer with non-existent NINO" should {
    "returns failed Future" in {
      val niResponse = testNIServiceWithMockHttp.getNIResponse(nonexistentnino)
      ScalaFutures.whenReady(niResponse.failed) { ex =>
        ex shouldBe a[NotFoundException]
      }
    }
  }
}
