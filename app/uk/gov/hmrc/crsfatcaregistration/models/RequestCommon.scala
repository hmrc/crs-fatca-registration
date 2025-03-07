/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.crsfatcaregistration.models

import play.api.libs.json.{Json, OFormat}

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZonedDateTime}
import java.util.UUID.randomUUID

case class RequestCommon(
  receiptDate: String,
  regime: String,
  acknowledgementReference: String,
  requestParameters: Option[Seq[RequestParameter]]
)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]

  def apply: RequestCommon = {
    val clock           = Clock.systemDefaultZone()
    val regime: String  = "CRFA"
    val acknRef: String = randomUUID().toString.replaceAll("-", "") // uuids are 36 and spec demands 32
    // Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val dateTime: String = ZonedDateTime
      .now(clock)
      .format(formatter)
    RequestCommon(dateTime, regime, acknRef, None)
  }

}
