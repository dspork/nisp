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

package uk.gov.hmrc.nisp.services.reference


import play.api.Logger
import uk.gov.hmrc.nisp.models.reference.EarningLevelModel
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._


object EarningLevelService {
  // scalastyle:off magic.number

  private lazy val earningLevels: Map[Int, EarningLevelModel] = {
    Map(
      2015 -> EarningLevelModel(2015, 112, 50,15300,1, 1),
      2014 -> EarningLevelModel(2014, 111, 50,15100,1, 1.009),
      2013 -> EarningLevelModel(2013, 109, 50,15000,1.009, 1.0272),
      2012 -> EarningLevelModel(2012, 107, 50,0,1.0272, 1.0457),
      2011 -> EarningLevelModel(2011, 102, 50,0,1.0457, 0),
      2010 -> EarningLevelModel(2010, 97, 50,0,0,0),
      2009 -> EarningLevelModel(2009, 95, 50,0,0,0),
      2008 -> EarningLevelModel(2008, 90, 50,0,0,0),
      2007 -> EarningLevelModel(2007, 87, 50,0,0,0),
      2006 -> EarningLevelModel(2006, 84, 50,0,0,0),
      2005 -> EarningLevelModel(2005, 82, 50,0,0,0),
      2004 -> EarningLevelModel(2004, 79, 50,0,0,0),
      2003 -> EarningLevelModel(2003, 77, 50,0,0,0),
      2002 -> EarningLevelModel(2002, 75, 50,0,0,0),
      2001 -> EarningLevelModel(2001, 72, 50,0,0,0),
      2000 -> EarningLevelModel(2000, 67, 50,0,0,0),
      1999 -> EarningLevelModel(1999, 66, 50,0,0,0),
      1998 -> EarningLevelModel(1998, 64, 50,0,0,0),
      1997 -> EarningLevelModel(1997, 62, 50,0,0,0),
      1996 -> EarningLevelModel(1996, 61, 50,0,0,0),
      1995 -> EarningLevelModel(1995, 58, 50,0,0,0),
      1994 -> EarningLevelModel(1994, 57, 50,0,0,0),
      1993 -> EarningLevelModel(1993, 56, 50,0,0,0),
      1992 -> EarningLevelModel(1992, 54, 50,0,0,0),
      1991 -> EarningLevelModel(1991, 52, 50,0,0,0),
      1990 -> EarningLevelModel(1990, 46, 50,0,0,0),
      1989 -> EarningLevelModel(1989, 43, 50,0,0,0),
      1988 -> EarningLevelModel(1988, 41, 50,0,0,0),
      1987 -> EarningLevelModel(1987, 39, 50,0,0,0),
      1986 -> EarningLevelModel(1986, 38, 19,0,0,0),
      1985 -> EarningLevelModel(1985, 35.50, 17.75,0,0,0),
      1984 -> EarningLevelModel(1984, 34, 17,0,0,0),
      1983 -> EarningLevelModel(1983, 32.50, 16.25,0,0,0),
      1982 -> EarningLevelModel(1982, 29.50, 14,0,0,0),
      1981 -> EarningLevelModel(1981, 27, 14,0,0,0),
      1980 -> EarningLevelModel(1980, 23, 14,0,0,0),
      1979 -> EarningLevelModel(1979, 19.50, 14,0,0,0),
      1978 -> EarningLevelModel(1978, 17.50, 14,0,0,0),
      1977 -> EarningLevelModel(1977, 15, 14,0,0,0),
      1976 -> EarningLevelModel(1976, 13, 14,0,0,0),
      1975 -> EarningLevelModel(1975, 11, 14,0,0,0)
    )
  }

  private def getEarningLevels(taxYear: Int): EarningLevelModel = {
    earningLevels.getOrElse(taxYear, {
      val (latestYear, latestValues) = earningLevels.toSeq.sortWith(_._1 > _._1).head
      Logger.warn(s"Cannot find Earning Levels for $taxYear, using $latestYear")
      latestValues
    })
  }

  def lowerEarningsLimit(taxYear: Int): BigDecimal = getEarningLevels(taxYear).lowEarningLevel

  def qualifyingLevel(taxYear: Int): BigDecimal = getEarningLevels(taxYear).qualifyingLevel

  def maxMarkup(taxYear: Int): BigDecimal = getEarningLevels(taxYear).maxMarkUp

  def lowerEarningThreshold(taxYear: Int): BigDecimal = getEarningLevels(taxYear).lowerEarningThreshold

  def s148Rate(taxYear: Int): BigDecimal = getEarningLevels(taxYear).s148Rate

  def rdaRate(taxYear: Int): BigDecimal = {
    if (getEarningLevels(taxYear).rdaRate <= 0) Logger.warn(s"$taxYear's RDA rate does not exist")
    getEarningLevels(taxYear).rdaRate
  }
}
