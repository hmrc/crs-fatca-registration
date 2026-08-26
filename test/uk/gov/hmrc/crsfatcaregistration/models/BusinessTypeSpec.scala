/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class BusinessTypeSpec extends AnyFreeSpec with Matchers {

  "BusinessType" - {

    "writes" - {

      "must write all business types to their expected JSON values" in {

        val businessTypes = Seq(
          partnerShip        -> "Partnership",
          limitedLiability   -> "LLP",
          corporateBody      -> "Corporate Body",
          unIncorporatedBody -> "Unincorporated Body",
          other              -> "Not Specified"
        )

        businessTypes.foreach {
          case (businessType, expectedValue) =>
            Json.toJson[BusinessType](businessType) mustBe JsString(expectedValue)
        }
      }
    }

    "reads" - {

      "must read Partnership" in {
        JsString("Partnership").validate[BusinessType] mustBe JsSuccess(partnerShip)
      }

      "must read LLP" in {
        JsString("LLP").validate[BusinessType] mustBe JsSuccess(limitedLiability)
      }

      "must read Corporate Body" in {
        JsString("Corporate Body").validate[BusinessType] mustBe JsSuccess(corporateBody)
      }

      "must read Unincorporated Body" in {
        JsString("Unincorporated Body").validate[BusinessType] mustBe JsSuccess(unIncorporatedBody)
      }

      "must read Not Specified" in {
        JsString("Not Specified").validate[BusinessType] mustBe JsSuccess(other)
      }

      "must fail to read an unknown business type" in {
        JsString("Unknown").validate[BusinessType] mustBe JsError()
      }

      "must fail to read a non-string JSON value" in {
        Json.obj("businessType" -> "Partnership").validate[BusinessType] mustBe JsError()
      }
    }
  }

}
