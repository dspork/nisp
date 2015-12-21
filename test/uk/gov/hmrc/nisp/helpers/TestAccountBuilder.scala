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

import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.domain.{Nino, Generator}

import scala.io.Source
import scala.util.Random

object TestAccountBuilder {

  val randomNino = () => Nino(new Generator(new Random()).nextNino.value.replaceFirst("MA", "QQ"))

  val nonExistentNino: Nino = randomNino()
  val excludedNino: Nino = randomNino()
  val regularNino: Nino = randomNino()
  val isleOfManNino: Nino = Nino(randomNino().value.replaceFirst("[A-Z]{2}", "MA"))

  val mappedTestAccounts = Map(
    excludedNino -> "excluded",
    regularNino -> "regular"
  )

  def jsonResponse(nino: Nino, api: String): HttpResponse = {
    val jsonFile = fileContents(s"test/resources/${mappedTestAccounts(nino)}/$api.json")
    HttpResponse(Status.OK, Some(Json.parse(jsonFile.replace("<NINO>", nino.value))))
  }

  private def fileContents(filename: String): String = Source.fromFile(filename).mkString
}
