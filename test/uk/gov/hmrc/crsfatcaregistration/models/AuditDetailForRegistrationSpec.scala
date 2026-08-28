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
import uk.gov.hmrc.crsfatcaregistration.models.audit.AuditDetailForRegistration

class AuditDetailForRegistrationSpec extends AnyFreeSpec with Matchers {

  private val auditDetail =
    AuditDetailForRegistration(
      affinityType = "Organisation",
      registeringAs = "Organisation",
      registrationType = "OrgWithID",
      idType = "UTR",
      idValue = "1234567890",
      tradingName = Some("Trading Name"),
      businessName = Some("Business Name"),
      addressLine1 = "1 Test Street",
      addressLine2 = Some("Test Area"),
      city = "London",
      region = Some("Greater London"),
      postcode = Some("AA1 1AA"),
      country = "GB",
      uprn = Some("123456789"),
      dateOfBirth = Some("1990-01-01"),
      firstContactName = "First Contact",
      firstContactEmail = "first@example.com",
      firstContactTelephone = Some("07123456789"),
      secondContactName = Some("Second Contact"),
      secondContactEmail = Some("second@example.com"),
      secondContactTelephone = Some("07987654321"),
      fatcaId = "FATCA123456"
    )

  "AuditDetailForRegistration" - {

    "must serialise to JSON" in {

      val result = Json.toJson(auditDetail)

      result mustBe Json.obj(
        "affinityType"           -> "Organisation",
        "registeringAs"          -> "Organisation",
        "registrationType"       -> "OrgWithID",
        "idType"                 -> "UTR",
        "idValue"                -> "1234567890",
        "tradingName"            -> "Trading Name",
        "businessName"           -> "Business Name",
        "addressLine1"           -> "1 Test Street",
        "addressLine2"           -> "Test Area",
        "city"                   -> "London",
        "region"                 -> "Greater London",
        "postcode"               -> "AA1 1AA",
        "country"                -> "GB",
        "uprn"                   -> "123456789",
        "dateOfBirth"            -> "1990-01-01",
        "firstContactName"       -> "First Contact",
        "firstContactEmail"      -> "first@example.com",
        "firstContactTelephone"  -> "07123456789",
        "secondContactName"      -> "Second Contact",
        "secondContactEmail"     -> "second@example.com",
        "secondContactTelephone" -> "07987654321",
        "fatcaId"                -> "FATCA123456"
      )
    }

    "must deserialise from JSON" in {

      val json = Json.obj(
        "affinityType"           -> "Organisation",
        "registeringAs"          -> "Organisation",
        "registrationType"       -> "OrgWithID",
        "idType"                 -> "UTR",
        "idValue"                -> "1234567890",
        "tradingName"            -> "Trading Name",
        "businessName"           -> "Business Name",
        "addressLine1"           -> "1 Test Street",
        "addressLine2"           -> "Test Area",
        "city"                   -> "London",
        "region"                 -> "Greater London",
        "postcode"               -> "AA1 1AA",
        "country"                -> "GB",
        "uprn"                   -> "123456789",
        "dateOfBirth"            -> "1990-01-01",
        "firstContactName"       -> "First Contact",
        "firstContactEmail"      -> "first@example.com",
        "firstContactTelephone"  -> "07123456789",
        "secondContactName"      -> "Second Contact",
        "secondContactEmail"     -> "second@example.com",
        "secondContactTelephone" -> "07987654321",
        "fatcaId"                -> "FATCA123456"
      )

      json.validate[AuditDetailForRegistration] mustBe JsSuccess(auditDetail)
    }

    "must serialise and deserialise when optional fields are empty" in {

      val auditDetailWithEmptyOptionalFields =
        auditDetail.copy(
          tradingName = None,
          businessName = None,
          addressLine2 = None,
          region = None,
          postcode = None,
          uprn = None,
          dateOfBirth = None,
          firstContactTelephone = None,
          secondContactName = None,
          secondContactEmail = None,
          secondContactTelephone = None
        )

      val json = Json.toJson(auditDetailWithEmptyOptionalFields)

      json.validate[AuditDetailForRegistration] mustBe JsSuccess(
        auditDetailWithEmptyOptionalFields
      )
    }
  }

}
