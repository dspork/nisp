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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.connectors.NpsConnector
import uk.gov.hmrc.nisp.models.SchemeMembership
import uk.gov.hmrc.nisp.models.nps.NpsDate
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import com.github.nscala_time.time.OrderingImplicits._
import org.joda.time.LocalDate
import uk.gov.hmrc.nisp.utils.NISPConstants

object SchemeMembershipService extends SchemeMembershipService {
  override val nps: NpsConnector = NpsConnector
}

trait SchemeMembershipService {
  val nps: NpsConnector

  def getSchemeSummary(nino: Nino)(implicit hc: HeaderCarrier): concurrent.Future[List[SchemeMembership]] = {
    val futureNpsSchemeMembership = nps.connectToSchemeMembership(nino)

    for(npsSchemeMembership <- futureNpsSchemeMembership) yield {
      npsSchemeMembership
        .groupBy(_.sequenceNumber)
        .map {
          case (seq, list) => list.maxBy(_.occurrenceNumber)
        }
        .map(sm => SchemeMembership(
          sm.startDate.localDate,
          sm.endDate.map(_.localDate).getOrElse(NISPConstants.contractedOutEndDate)
        ))
        .toList
        .sortBy(_.schemeStartDate).reverse
    }
  }
}
