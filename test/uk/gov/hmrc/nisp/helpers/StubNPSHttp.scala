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

package uk.gov.hmrc.nisp.helpers

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nisp.helpers.TestAccountBuilder._
import uk.gov.hmrc.play.http.{BadGatewayException, HttpGet, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

object StubNPSHttp extends UnitSpec with MockitoSugar {
  val stubHttp = mock[HttpGet]

  stubNotFound(nonExistentNino)
  stubFailure(isleOfManNino)
  stubSuccess(regularNino)
  stubSuccess(excludedNino)
  stubSuccess(contractedOutNino)
  stubSuccess(psodNino)

  private def stubSuccess(urlEndsWith: String, response: Future[HttpResponse]): Unit =
    when(stubHttp.GET[HttpResponse](Matchers.endsWith(urlEndsWith))(Matchers.any(), Matchers.any())).thenReturn(response)

  private def stubUnsuccessul(urlEndsWith: String, ex: Exception): Unit =
    when(stubHttp.GET[HttpResponse](Matchers.endsWith(urlEndsWith))(Matchers.any(), Matchers.any())).thenReturn(Future.failed(ex))

  private def stubSuccess(nino: Nino): Unit = {
    val ninoWithoutSuffix = unsuffixedNino(nino)

    stubSuccess(s"/pensions/$ninoWithoutSuffix/sp_summary", jsonResponse(nino, "summary"))
    stubSuccess(s"/pensions/$ninoWithoutSuffix/ni_record", jsonResponse(nino, "nirecord"))
    stubSuccess(s"/pensions/$ninoWithoutSuffix/scheme", jsonResponse(nino, "schememembership"))
    stubSuccess(s"/pensions/$ninoWithoutSuffix/liabilities", jsonResponse(nino, "liabilities"))
  }

  private def unsuffixedNino(nino: Nino): String = {
    nino.value.substring(0, 8)
  }

  private def stubNotFound(nino: Nino): Unit = {
    val ninoWithoutSuffix = unsuffixedNino(nino)
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/sp_summary", new NotFoundException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/ni_record", new NotFoundException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/scheme", new NotFoundException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/liabilities", new NotFoundException(""))
  }

  private def stubFailure(nino: Nino): Unit = {
    val ninoWithoutSuffix = unsuffixedNino(nino)
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/sp_summary", new BadGatewayException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/ni_record", new BadGatewayException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/scheme", new BadGatewayException(""))
    stubUnsuccessul(s"/pensions/$ninoWithoutSuffix/liabilities", new BadGatewayException(""))
  }
}
