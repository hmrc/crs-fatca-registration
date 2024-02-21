/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json._

sealed trait BusinessType {
  val value: String
}

case object partnerShip extends BusinessType {
  override val value: String = "Partnership"
}

case object limitedLiability extends BusinessType {
  override val value: String = "LLP"
}

case object corporateBody extends BusinessType {
  override val value: String = "Corporate Body"
}

case object unIncorporatedBody extends BusinessType {
  override val value: String = "Unincorporated Body"
}

case object other extends BusinessType {
  override val value: String = "Not Specified"
}

object BusinessType {

  implicit lazy val writes: Writes[BusinessType] = Writes {
    businessType =>
      JsString(businessType.value)
  }

  implicit lazy val reads: Reads[BusinessType] = {
    case JsString("Partnership")         => JsSuccess(partnerShip)
    case JsString("LLP")                 => JsSuccess(limitedLiability)
    case JsString("Corporate Body")      => JsSuccess(corporateBody)
    case JsString("Unincorporated Body") => JsSuccess(unIncorporatedBody)
    case JsString("Not Specified")       => JsSuccess(other)
    case _                               => JsError()
  }

}
