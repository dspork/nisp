National Insurance and State Pension
====================================

[![Build Status](https://travis-ci.org/hmrc/nisp.svg)](https://travis-ci.org/hmrc/nisp) [ ![Download](https://api.bintray.com/packages/hmrc/releases/nisp/images/download.svg) ](https://bintray.com/hmrc/releases/nisp/_latestVersion)

This microservice retrieves data from a [HoD](http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm) system called "NPS", for calculating State Pension Age and Amount.

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) to run.

API
---

####   State Pension Summary

Fetches the SPResponseModel object for the frontend service.

* **URL**

  `nisp/:nino/spsummary`

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `nino=[nino]`

  The NINO given must be a valid NINO: ([http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm](http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm))

* **Success Response:**

  * **Code:** 200 <br />
    **Content:**

```json
{
    "spSummary":{
        "nino":"QQ000000A",
        "lastProcessedDate":"05/04/2014",
        "statePensionAmount":{
            "week":154.94,
            "month":673.71,
            "year":8084.55
        },
        "statePensionAge":{
            "age":65,
            "date":"06/09/2019"
        },
        "contextMessage":"ScenarioOne",
        "finalRelevantYear":2018,
        "numberOfQualifyingYears":35,
        "numberOfGaps":10,
        "numberOfGapsPayable":4,
        "yearsToContributeUntilPensionAge":5,
        "hasPsod":false,
        "dateOfBirth":"09/03/1954",
        "forecastAmount":{
            "week":159.62,
            "month":694.06,
            "year":8328.74
        },
        "fullNewStatePensionAmount":151.25,
        "contractedOutFlag": false,
        "customerAge": 61,
        "copeAmount":{
            "week": 0.00,
            "month": 0.00,
            "year": 0.00
        }
    }
}
```

* **Error Response:**

  * **Code:** 404 NOT FOUND
    **Reason:** NINO could not be found
    **Content:** ```json{}```

* **Other Response:**

  * **Code:** 200
    **Reason:** User is excluded from service
    **Content:**

```json
{
    "spExclusions":{
        "spExclusions":["NorthernIreland"]
    }
}
```

####   National Insurance Record

Fetches the NIResponseModel object for the frontend service.

* **URL**

  `nisp/:nino/nirecord`

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `nino=[nino]`

  The NINO given must be a valid NINO: ([http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm](http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm))

* **Success Response:**

  * **Code:** 200 <br />
    **Content:**

```json
{
  "niRecord": {
    "taxYears": [
      {
        "taxYear": 1975,
        "qualifying": true,
        "classOneContributions": 70.67,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 26,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 1976,
        "qualifying": true,
        "classOneContributions": 53.5,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 34,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 1977,
        "qualifying": true,
        "classOneContributions": 82.13,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 28,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 1978,
        "qualifying": true,
        "classOneContributions": 69.35,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 41,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 1979,
        "qualifying": true,
        "classOneContributions": 24.9,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 42,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 1980,
        "qualifying": true,
        "classOneContributions": 114.19,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 35,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
...
      {
        "taxYear": 2010,
        "qualifying": false,
        "classOneContributions": 0,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 4,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 2011,
        "qualifying": false,
        "classOneContributions": 0,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 0,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      },
      {
        "taxYear": 2012,
        "qualifying": false,
        "classOneContributions": 0,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 12,
        "classThreePayable" : 638.00,
        "classThreePayableBy": 01/01/2022,
        "payable": true
      },
      {
        "taxYear": 2013,
        "qualifying": true,
        "classOneContributions": 2430.24,
        "classTwoCredits": 0,
        "classThreeCredits": 0,
        "otherCredits": 0,
        "classThreePayable" : null,
        "classThreePayableBy": null,
        "payable": false
      }
    ]
  },
  "niSummary": {
    "noOfQualifyingYears": 35,
    "noOfNonQualifyingYears": 10,
    "yearsToContributeUntilPensionAge": 5,
    "spaYear": 2019,
    "earningsIncludedUpTo": "05\/04\/2014",
    "unavailableYear": 2014,
    "pre75QualifyingYears": 5,
    "numberOfPayableGaps": 4,
    "numberOfNonPayableGaps": 6,
    "canImproveWithGaps": false
  }
}
```

* **Error Response:**

  * **Code:** 404 NOT FOUND
    **Reason:** NINO could not be found
    **Content:** ```json{}```

* **Other Response:**

  * **Code:** 200
    **Reason:** User is excluded from service
    **Content:**

```json
{
    "spExclusions":{
        "spExclusions":["NorthernIreland"]
    }
}
```

Configuration
---

This service requires configuration for other services, for example NPS requires:

| *Key*                                    | *Description*                   |
| ---------------------------------------- | ---------------------------     |
| `microservice.services.nps-hod.protocol` | The protocol of the NPS service |
| `microservice.services.nps-hod.host`     | The host of the NPS service     |
| `microservice.services.nps-hod.port`     | The port of the NPS service     |

License
---

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

