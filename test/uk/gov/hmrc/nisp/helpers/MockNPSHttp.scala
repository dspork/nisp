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

package uk.gov.hmrc.nisp.helpers

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{NotFoundException, HttpResponse, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.io.Source

object MockNPSHttp extends UnitSpec with MockitoSugar {
  val mockHttp = mock[HttpGet]
  val ninos = List(TestAccountBuilder.excludedNino, TestAccountBuilder.regularNino)

  def createMockedURL(urlEndsWith: String, response: Future[HttpResponse]): Unit =
    when(mockHttp.GET[HttpResponse](Matchers.endsWith(urlEndsWith))(Matchers.any(), Matchers.any())).thenReturn(response)

  def createFailedMockedURL(urlEndsWith: String): Unit =
    when(mockHttp.GET[HttpResponse](Matchers.endsWith(urlEndsWith))(Matchers.any(), Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))

  def setupNinoEndpoints(nino: String): Unit = {
    val ninoWithoutSuffix = nino.substring(0,8)

    createMockedURL(s"/pensions/$ninoWithoutSuffix/sp_summary", TestAccountBuilder.jsonResponse(nino, "summary"))
    createMockedURL(s"/pensions/$ninoWithoutSuffix/ni_record", TestAccountBuilder.jsonResponse(nino, "nirecord"))
    createMockedURL(s"/pensions/$ninoWithoutSuffix/scheme", TestAccountBuilder.jsonResponse(nino, "schememembership"))
    createMockedURL(s"/pensions/$ninoWithoutSuffix/liabilities", TestAccountBuilder.jsonResponse(nino, "liabilities"))
  }

  ninos.foreach(setupNinoEndpoints)

  val nonExistentNino = TestAccountBuilder.nonExistentNino
  createFailedMockedURL(s"/pensions/$nonExistentNino/sp_summary")
  createFailedMockedURL(s"/pensions/$nonExistentNino/ni_record")
  createFailedMockedURL(s"/pensions/$nonExistentNino/scheme")
  createFailedMockedURL(s"/pensions/$nonExistentNino/liabilities")
}
