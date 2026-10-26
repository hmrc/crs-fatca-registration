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
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.crsfatcaregistration.models.audit.UpdateContactDetails

class UpdateContactDetailsSpec extends AnyFreeSpec with Matchers {

  private val auditDetail =
    UpdateContactDetails(
      fatcaId = "FATCA123456",
      affinityType = "Organisation",
      name = Some("John Smith"),
      email = Some("john@example.com"),
      telephone = Some("07123456789"),
      firstContactName = Some("First Contact"),
      firstContactEmail = Some("first@example.com"),
      firstContactTelephone = Some("07123456789"),
      secondContactName = Some("Second Contact"),
      secondContactEmail = Some("second@example.com"),
      secondContactTelephone = Some("07987654321")
    )

  "UpdateContactDetails" - {

    "must serialise to JSON" in {

      val result = Json.toJson(auditDetail)

      result mustBe Json.obj(
        "fatcaId"                -> "FATCA123456",
        "affinityType"           -> "Organisation",
        "name"                   -> "John Smith",
        "email"                  -> "john@example.com",
        "telephone"              -> "07123456789",
        "firstContactName"       -> "First Contact",
        "firstContactEmail"      -> "first@example.com",
        "firstContactTelephone"  -> "07123456789",
        "secondContactName"      -> "Second Contact",
        "secondContactEmail"     -> "second@example.com",
        "secondContactTelephone" -> "07987654321"
      )
    }

    "must deserialise from JSON" in {

      val json = Json.obj(
        "fatcaId"                -> "FATCA123456",
        "affinityType"           -> "Organisation",
        "name"                   -> "John Smith",
        "email"                  -> "john@example.com",
        "telephone"              -> "07123456789",
        "firstContactName"       -> "First Contact",
        "firstContactEmail"      -> "first@example.com",
        "firstContactTelephone"  -> "07123456789",
        "secondContactName"      -> "Second Contact",
        "secondContactEmail"     -> "second@example.com",
        "secondContactTelephone" -> "07987654321"
      )

      json.validate[UpdateContactDetails] mustBe JsSuccess(auditDetail)
    }

    "must omit optional fields when they are None" in {

      val auditDetailWithoutOptionalFields =
        UpdateContactDetails(
          fatcaId = "FATCA123456",
          affinityType = "Individual",
          email = Some("john@example.com")
        )

      val json = Json.toJson(auditDetailWithoutOptionalFields)

      json mustBe Json.obj(
        "fatcaId"      -> "FATCA123456",
        "affinityType" -> "Individual",
        "email"        -> "john@example.com"
      )

      json.validate[UpdateContactDetails] mustBe JsSuccess(
        auditDetailWithoutOptionalFields
      )
    }

    "must serialise and deserialise with no optional fields" in {

      val auditDetailWithNoOptionalFields =
        UpdateContactDetails(
          fatcaId = "FATCA123456",
          affinityType = "Organisation"
        )

      val json = Json.toJson(auditDetailWithNoOptionalFields)

      json mustBe Json.obj(
        "fatcaId"      -> "FATCA123456",
        "affinityType" -> "Organisation"
      )

      json.validate[UpdateContactDetails] mustBe JsSuccess(
        auditDetailWithNoOptionalFields
      )
    }

    "must fail validation when mandatory fields are missing" in {

      val missingFatcaId = Json.obj(
        "affinityType" -> "Organisation"
      )

      val missingAffinityType = Json.obj(
        "fatcaId" -> "FATCA123456"
      )

      missingFatcaId.validate[UpdateContactDetails].isError mustBe true

      missingAffinityType.validate[UpdateContactDetails].isError mustBe true
    }
  }

}
