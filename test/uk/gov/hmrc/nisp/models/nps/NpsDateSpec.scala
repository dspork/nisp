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

package uk.gov.hmrc.nisp.models.nps

import org.joda.time.LocalDate
import uk.gov.hmrc.play.test.UnitSpec

class NpsDateSpec extends UnitSpec {
  "taxYearRange calculation" should {
    "return range(2001,2002) for dates 06/04/2001 and 05/04/2003" in {
      NpsDate(new LocalDate(2001,4,6)).taxYearsUntil(NpsDate(new LocalDate(2003,4,5))) shouldBe Range.inclusive(2001,2002)
    }

    "return range(2000,2002) for dates 05/04/2001 and 05/04/2003" in {
      NpsDate(new LocalDate(2001,4,5)).taxYearsUntil(NpsDate(new LocalDate(2003,4,5))) shouldBe Range.inclusive(2000,2002)
    }

    "return range(2001,2003) for dates 06/04/2001 and 06/04/2003" in {
      NpsDate(new LocalDate(2001,4,6)).taxYearsUntil(NpsDate(new LocalDate(2003,4,6))) shouldBe Range.inclusive(2001,2003)
    }
  }

  "class2TaxYearRange calculation" should {
    "return range(2014) for dates 05/04/2015 and 11/04/2015" in {
      NpsDate(new LocalDate(2015,4,5)).classTwoTaxYearsUntil(NpsDate(new LocalDate(2015,4,11))) shouldBe Range.inclusive(2014,2014)
    }

    "return range(2014) for dates 05/04/2015 and 12/04/2015" in {
      NpsDate(new LocalDate(2015,4,5)).classTwoTaxYearsUntil(NpsDate(new LocalDate(2015,4,12))) shouldBe Range.inclusive(2014,2015)
    }

    "return range(2014) for dates 05/04/2015 and 10/04/2015" in {
      NpsDate(new LocalDate(2015,4,5)).classTwoTaxYearsUntil(NpsDate(new LocalDate(2015,4,10))) shouldBe Range.inclusive(2014,2014)
    }

    "return range(2014) for dates 05/04/2015 and 06/04/2015" in {
      NpsDate(new LocalDate(2015,4,5)).classTwoTaxYearsUntil(NpsDate(new LocalDate(2015,4,6))) shouldBe Range.inclusive(2014,2014)
    }

    "return range(2014) for dates 05/04/2015 and 05/04/2015" in {
      NpsDate(new LocalDate(2015,4,5)).classTwoTaxYearsUntil(NpsDate(new LocalDate(2015,4,5))) shouldBe Range.inclusive(2014,2014)
    }
  }

  "max functions" should {
    "return 02/01/2015 for 02/01/2015 and 01/01/2015" in {
      NpsDate(new LocalDate(2015,1,2)).max(NpsDate(new LocalDate(2015,1,1))) shouldBe NpsDate(new LocalDate(2015,1,2))
    }

    "return 02/01/2015 for 02/01/2015 and 02/01/2015" in {
      NpsDate(new LocalDate(2015,1,2)).max(NpsDate(new LocalDate(2015,1,2))) shouldBe NpsDate(new LocalDate(2015,1,2))
    }

    "return 02/01/2015 for 01/01/2015 and 02/01/2015" in {
      NpsDate(new LocalDate(2015,1,1)).max(NpsDate(new LocalDate(2015,1,2))) shouldBe NpsDate(new LocalDate(2015,1,2))
    }
  }

  "min functions" should {
    "return 01/01/2015 for 02/01/2015 and 01/01/2015" in {
      NpsDate(new LocalDate(2015,1,2)).min(NpsDate(new LocalDate(2015,1,1))) shouldBe NpsDate(new LocalDate(2015,1,1))
    }

    "return 02/01/2015 for 02/01/2015 and 02/01/2015" in {
      NpsDate(new LocalDate(2015,1,2)).min(NpsDate(new LocalDate(2015,1,2))) shouldBe NpsDate(new LocalDate(2015,1,2))
    }

    "return 01/01/2015 for 01/01/2015 and 02/01/2015" in {
      NpsDate(new LocalDate(2015,1,1)).min(NpsDate(new LocalDate(2015,1,2))) shouldBe NpsDate(new LocalDate(2015,1,1))
    }
  }

  "roundUpToDay" should {
    "return 08/03/2015 for round to Sunday for dates 07/03/2015, 06/03/2015, 05/03/2015, 04/03/2015, 03/03/2015, 02/03/2015" in {
      NpsDate(2015,3,7).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
      NpsDate(2015,3,6).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
      NpsDate(2015,3,5).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
      NpsDate(2015,3,4).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
      NpsDate(2015,3,3).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
      NpsDate(2015,3,2).roundUpToDay(7) shouldBe NpsDate(2015,3,8)
    }

    "return 07/03/2015 for round to Saturday for dates 06/03/2015, 05/03/2015, 04/03/2015, 03/03/2015, 02/03/2015, 01/03/2015" in {
      NpsDate(2015,3,6).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
      NpsDate(2015,3,5).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
      NpsDate(2015,3,4).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
      NpsDate(2015,3,3).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
      NpsDate(2015,3,2).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
      NpsDate(2015,3,1).roundUpToDay(6) shouldBe NpsDate(2015,3,7)
    }

    "return 06/03/2015 for round to Friday for dates 06/03/2015, 05/03/2015, 04/03/2015, 03/03/2015, 02/03/2015, 01/03/2015" in {
      NpsDate(2015,3,5).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
      NpsDate(2015,3,4).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
      NpsDate(2015,3,3).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
      NpsDate(2015,3,2).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
      NpsDate(2015,3,1).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
      NpsDate(2015,2,28).roundUpToDay(5) shouldBe NpsDate(2015,3,6)
    }

    "return 05/03/2015 for round to Thursday for dates 04/03/2015, 03/03/2015, 02/03/2015, 01/03/2015, 28/02/2015, 27/02/2015" in {
      NpsDate(2015,3,4).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
      NpsDate(2015,3,3).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
      NpsDate(2015,3,2).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
      NpsDate(2015,3,1).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
      NpsDate(2015,2,28).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
      NpsDate(2015,2,27).roundUpToDay(4) shouldBe NpsDate(2015,3,5)
    }

    "return 04/03/2015 for round to Wednesday for dates 03/03/2015, 02/03/2015, 01/03/2015, 28/02/2015, 27/02/2015, 26/02/2015" in {
      NpsDate(2015,3,3).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
      NpsDate(2015,3,2).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
      NpsDate(2015,3,1).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
      NpsDate(2015,2,28).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
      NpsDate(2015,2,27).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
      NpsDate(2015,2,26).roundUpToDay(3) shouldBe NpsDate(2015,3,4)
    }

    "return 03/03/2015 for round to Tuesday for dates 02/03/2015, 01/03/2015, 28/02/2015, 27/02/2015, 26/02/2015, 25/02/2015" in {
      NpsDate(2015,3,2).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
      NpsDate(2015,3,1).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
      NpsDate(2015,2,28).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
      NpsDate(2015,2,27).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
      NpsDate(2015,2,26).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
      NpsDate(2015,2,25).roundUpToDay(2) shouldBe NpsDate(2015,3,3)
    }

    "return 02/03/2015 for round to Monday for dates 01/03/2015, 28/02/2015, 27/02/2015, 26/02/2015, 25/02/2015, 24/02/2015" in {
      NpsDate(2015,3,1).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
      NpsDate(2015,2,28).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
      NpsDate(2015,2,27).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
      NpsDate(2015,2,26).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
      NpsDate(2015,2,25).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
      NpsDate(2015,2,24).roundUpToDay(1) shouldBe NpsDate(2015,3,2)
    }
  }

  "taxYearEndDate" should {
    "return 05/04/2015 for 2015/12/12" in {
      NpsDate(2014, 12, 12).taxYearEndDate shouldBe NpsDate(2015, 4, 5)
    }

    "return 05/04/2015 for 2014/04/06" in {
      NpsDate(2014, 4, 6).taxYearEndDate shouldBe NpsDate(2015, 4, 5)
    }

    "return 05/04/2014 for 2014/04/05" in {
      NpsDate(2014, 4, 5).taxYearEndDate shouldBe NpsDate(2014, 4, 5)
    }
  }

  "lastProcessedTaxYear" should {
    "return 2013 for 6/4/2015" in {
      NpsDate(2015,4,6).lastProcessedTaxYear shouldBe 2013
    }

    "return 2012 for 5/4/2015" in {
      NpsDate(2015,4,5).lastProcessedTaxYear shouldBe 2012
    }

    "return 2012 for 1/10/2014" in {
      NpsDate(2015,4,5).lastProcessedTaxYear shouldBe 2012
    }

    "return 2012 for 30/09/2014" in {
      NpsDate(2014,9,30).lastProcessedTaxYear shouldBe 2012
    }
  }
}
