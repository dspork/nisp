[Check your State Pension](https://www.gov.uk/check-state-pension) backend microservice (legacy)
====================================

This is a mirror of the source code of the nisp microservice which has been decommissioned and removed from HMRC repositories and environments.

This fork exists in case anyone finds the commit history useful.  It is no longer being developed upon;
nisp was split into two domain-driven microservices [state-pension](https://github.com/hmrc/state-pension) and [national-insurance-record.](https://github.com/hmrc/national-insurance-record)

Purpose
------------

This microservice retrieves data from a [HoD] system called [NPS], for calculating State Pension Age and Amount.

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and needs at least [JRE] 8  to run.

Configuration
---

This service requires configuration for other services, for example [NPS] requires:

| *Key*                                    | *Description*                   |
| ---------------------------------------- | ---------------------------     |
| `microservice.services.nps-hod.protocol` | The protocol of the NPS service |
| `microservice.services.nps-hod.host`     | The host of the NPS service     |
| `microservice.services.nps-hod.port`     | The port of the NPS service     |

Acronyms
---

In the context of this application we use the following acronyms and define their 
meanings. Provided you will also find a web link to discover more about the systems
and technology. 

* [API]: Application Programming Interface

* [HoD]: Head of Duty

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [NI]: National Insurance 

* [NINO]: National Insurance Number

* [NPS]: National Insurance and Pay As You Earn Service

* [URL]: Uniform Resource Locator

License
---

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

[NPS]: http://www.publications.parliament.uk/pa/cm201012/cmselect/cmtreasy/731/73107.htm
[HoD]: http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm
[NINO]: http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm
[NI]: https://www.gov.uk/national-insurance/overview
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
[JSON]: http://www.json.org/
