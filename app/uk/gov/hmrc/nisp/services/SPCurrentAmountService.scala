/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.nisp.models.nps.{NpsAmountA2016, NpsAmountB2016, NpsSummaryModel}

object SPCurrentAmountService {
  def calculate(amountA: NpsAmountA2016, amountB: NpsAmountB2016): BigDecimal = amountA.total.max(amountB.mainComponent)
}
