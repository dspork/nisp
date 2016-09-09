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
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder
import uk.gov.hmrc.nisp.models.SchemeMembership
import uk.gov.hmrc.nisp.models.nps.{NpsDate, NpsSchemeMembership}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SchemeMembershipServiceSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  val nino = TestAccountBuilder.randomNino()
  implicit val hc = HeaderCarrier()

  val stubNpsConnector = mock[NpsConnector]

  val stubSchemeMembershipService = new SchemeMembershipService {
    override val nps: NpsConnector = stubNpsConnector
  }

  "SchemeMembershipService should" should {
    "sort the scheme memberships is reverse date order by start date" in {
      when(stubNpsConnector.connectToSchemeMembership(Matchers.any())(Matchers.any())).thenReturn(
        Future.successful(List(
          NpsSchemeMembership(NpsDate(1978, 4, 6), Some(NpsDate(1983, 3, 5)), 1, 1),
          NpsSchemeMembership(NpsDate(1980, 4, 7), Some(NpsDate(1983, 4, 5)), 2, 1),
          NpsSchemeMembership(NpsDate(1980, 4, 6), Some(NpsDate(1982, 4, 5)), 3, 1),
          NpsSchemeMembership(NpsDate(2000, 4, 6), Some(NpsDate(2002, 4, 5)), 4, 1)
        ))
      )

      ScalaFutures.whenReady(stubSchemeMembershipService.getSchemeSummary(nino)(hc)) {
        list => list shouldBe
          List(
            SchemeMembership(new LocalDate(2000, 4, 6), new LocalDate(2002, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 7), new LocalDate(1983, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 6), new LocalDate(1982, 4, 5)),
            SchemeMembership(new LocalDate(1978, 4, 6), new LocalDate(1983, 3, 5))
          )
      }
    }

    "return 5th April 2016 if the Scheme does not have an end date" in {
      when(stubNpsConnector.connectToSchemeMembership(Matchers.any())(Matchers.any())).thenReturn(
        Future.successful(List(
          NpsSchemeMembership(NpsDate(2000, 4, 6), None, 1, 1),
          NpsSchemeMembership(NpsDate(1980, 4, 7), None, 2, 1)
        ))
      )

      ScalaFutures.whenReady(stubSchemeMembershipService.getSchemeSummary(nino)(hc)) {
        list => list shouldBe
          List(
            SchemeMembership(new LocalDate(2000, 4, 6), new LocalDate(2016, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 7), new LocalDate(2016, 4, 5))
          )
      }
    }

    "group the list by sequence number" in {
      when(stubNpsConnector.connectToSchemeMembership(Matchers.any())(Matchers.any())).thenReturn(
        Future.successful(List(
            NpsSchemeMembership(NpsDate(1978, 4, 6), Some(NpsDate(1983, 3, 5)), 1, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 6), Some(NpsDate(1982, 4, 5)), 2, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 6), Some(NpsDate(1982, 4, 5)), 2, 2),
            NpsSchemeMembership(NpsDate(1980, 4, 7), Some(NpsDate(1983, 4, 5)), 3, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 7), Some(NpsDate(1983, 4, 5)), 3, 2),
            NpsSchemeMembership(NpsDate(2000, 4, 6), Some(NpsDate(2002, 4, 5)), 4, 1)
        ))
      )

      ScalaFutures.whenReady(stubSchemeMembershipService.getSchemeSummary(nino)(hc)) {
        list => list shouldBe
          List(
            SchemeMembership(new LocalDate(2000, 4, 6), new LocalDate(2002, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 7), new LocalDate(1983, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 6), new LocalDate(1982, 4, 5)),
            SchemeMembership(new LocalDate(1978, 4, 6), new LocalDate(1983, 3, 5))
          )
      }
    }

    "when grouping use the LATEST occurence number in the sequence" in {
      when(stubNpsConnector.connectToSchemeMembership(Matchers.any())(Matchers.any())).thenReturn(
        Future.successful(List(
            NpsSchemeMembership(NpsDate(1978, 4, 6), Some(NpsDate(1983, 3, 5)), 1, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 6), Some(NpsDate(1982, 4, 5)), 2, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 6), Some(NpsDate(1982, 4, 6)), 2, 2),
            NpsSchemeMembership(NpsDate(1980, 4, 7), Some(NpsDate(1983, 4, 6)), 3, 1),
            NpsSchemeMembership(NpsDate(1980, 4, 7), Some(NpsDate(1983, 4, 5)), 3, 2),
            NpsSchemeMembership(NpsDate(2000, 4, 6), Some(NpsDate(2002, 4, 5)), 4, 1)
        ))
      )

      ScalaFutures.whenReady(stubSchemeMembershipService.getSchemeSummary(nino)(hc)) {
        list => list shouldBe
          List(
            SchemeMembership(new LocalDate(2000, 4, 6), new LocalDate(2002, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 7), new LocalDate(1983, 4, 5)),
            SchemeMembership(new LocalDate(1980, 4, 6), new LocalDate(1982, 4, 6)),
            SchemeMembership(new LocalDate(1978, 4, 6), new LocalDate(1983, 3, 5))
          )
      }
    }

  }
}
