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
import play.api.libs.json.{JsError, JsSuccess, Json}

class ContactInformationSpec extends AnyFreeSpec with Matchers {

  private val organisationDetails =
    OrganisationDetails(
      name = "Test Organisation"
    )

  private val individualDetails =
    IndividualDetails(
      firstName = "Jack",
      middleName = Some("Test"),
      lastName = "Witchell"
    )

  private val organisationContact =
    ContactInformationForOrganisation(
      organisation = organisationDetails,
      email = "organisation@example.com",
      phone = Some("01234567890"),
      mobile = Some("07123456789")
    )

  private val individualContact =
    ContactInformationForIndividual(
      individual = individualDetails,
      email = "individual@example.com",
      phone = Some("01234567890"),
      mobile = Some("07123456789")
    )

  "OrganisationDetails" - {

    "must serialise and deserialise" in {

      val json = Json.toJson(organisationDetails)

      json.validate[OrganisationDetails] mustBe JsSuccess(organisationDetails)
    }
  }

  "IndividualDetails" - {

    "must serialise and deserialise" in {

      val json = Json.toJson(individualDetails)

      json.validate[IndividualDetails] mustBe JsSuccess(individualDetails)
    }

    "must serialise and deserialise when middle name is absent" in {

      val individualWithoutMiddleName =
        individualDetails.copy(
          middleName = None
        )

      val json = Json.toJson(individualWithoutMiddleName)

      json.validate[IndividualDetails] mustBe JsSuccess(
        individualWithoutMiddleName
      )
    }
  }

  "ContactInformationForIndividual" - {

    "must serialise and deserialise" in {

      val json = Json.toJson(individualContact)

      json.validate[ContactInformationForIndividual] mustBe JsSuccess(
        individualContact
      )
    }

    "must serialise and deserialise when optional contact numbers are absent" in {

      val contact =
        individualContact.copy(
          phone = None,
          mobile = None
        )

      val json = Json.toJson(contact)

      json.validate[ContactInformationForIndividual] mustBe JsSuccess(contact)
    }
  }

  "ContactInformationForOrganisation" - {

    "must serialise and deserialise" in {

      val json = Json.toJson(organisationContact)

      json.validate[ContactInformationForOrganisation] mustBe JsSuccess(
        organisationContact
      )
    }

    "must serialise and deserialise when optional contact numbers are absent" in {

      val contact =
        organisationContact.copy(
          phone = None,
          mobile = None
        )

      val json = Json.toJson(contact)

      json.validate[ContactInformationForOrganisation] mustBe JsSuccess(contact)
    }
  }

  "PrimaryContact" - {

    "reads" - {

      "must read an organisation contact" in {

        val json = Json.obj(
          "organisation" -> organisationDetails,
          "email"        -> "organisation@example.com",
          "phone"        -> "01234567890",
          "mobile"       -> "07123456789"
        )

        json.validate[PrimaryContact] mustBe JsSuccess(
          PrimaryContact(organisationContact)
        )
      }

      "must read an individual contact" in {

        val json = Json.obj(
          "individual" -> individualDetails,
          "email"      -> "individual@example.com",
          "phone"      -> "01234567890",
          "mobile"     -> "07123456789"
        )

        json.validate[PrimaryContact] mustBe JsSuccess(
          PrimaryContact(individualContact)
        )
      }

      "must read a contact when phone and mobile are absent" in {

        val json = Json.obj(
          "individual" -> individualDetails,
          "email"      -> "individual@example.com"
        )

        json.validate[PrimaryContact] mustBe JsSuccess(
          PrimaryContact(
            individualContact.copy(
              phone = None,
              mobile = None
            )
          )
        )
      }

      "must throw when neither organisation nor individual is supplied" in {

        val json = Json.obj(
          "email" -> "contact@example.com"
        )

        val exception =
          intercept[Exception] {
            json.validate[PrimaryContact]
          }

        exception.getMessage mustBe
          "Primary Contact must have either an organisation or individual element"
      }

      "must throw when both organisation and individual are supplied" in {

        val json = Json.obj(
          "organisation" -> organisationDetails,
          "individual"   -> individualDetails,
          "email"        -> "contact@example.com"
        )

        val exception =
          intercept[Exception] {
            json.validate[PrimaryContact]
          }

        exception.getMessage mustBe
          "Primary Contact must have either an organisation or individual element"
      }
    }

    "writes" - {

      "must write an organisation contact" in {

        Json.toJson(
          PrimaryContact(organisationContact)
        ) mustBe Json.toJson(organisationContact)
      }

      "must write an individual contact" in {

        Json.toJson(
          PrimaryContact(individualContact)
        ) mustBe Json.toJson(individualContact)
      }
    }
  }

  "SecondaryContact" - {

    "reads" - {

      "must read an organisation contact" in {

        val json = Json.obj(
          "organisation" -> organisationDetails,
          "email"        -> "organisation@example.com",
          "phone"        -> "01234567890",
          "mobile"       -> "07123456789"
        )

        json.validate[SecondaryContact] mustBe JsSuccess(
          SecondaryContact(organisationContact)
        )
      }

      "must read an individual contact" in {

        val json = Json.obj(
          "individual" -> individualDetails,
          "email"      -> "individual@example.com",
          "phone"      -> "01234567890",
          "mobile"     -> "07123456789"
        )

        json.validate[SecondaryContact] mustBe JsSuccess(
          SecondaryContact(individualContact)
        )
      }

      "must read a contact when phone and mobile are absent" in {

        val json = Json.obj(
          "organisation" -> organisationDetails,
          "email"        -> "organisation@example.com"
        )

        json.validate[SecondaryContact] mustBe JsSuccess(
          SecondaryContact(
            organisationContact.copy(
              phone = None,
              mobile = None
            )
          )
        )
      }

      "must throw when neither organisation nor individual is supplied" in {

        val json = Json.obj(
          "email" -> "contact@example.com"
        )

        val exception =
          intercept[Exception] {
            json.validate[SecondaryContact]
          }

        exception.getMessage mustBe
          "Secondary Contact must have either an organisation or individual element"
      }

      "must throw when both organisation and individual are supplied" in {

        val json = Json.obj(
          "organisation" -> organisationDetails,
          "individual"   -> individualDetails,
          "email"        -> "contact@example.com"
        )

        val exception =
          intercept[Exception] {
            json.validate[SecondaryContact]
          }

        exception.getMessage mustBe
          "Secondary Contact must have either an organisation or individual element"
      }
    }

    "writes" - {

      "must write an organisation contact" in {

        Json.toJson(
          SecondaryContact(organisationContact)
        ) mustBe Json.toJson(organisationContact)
      }

      "must write an individual contact" in {

        Json.toJson(
          SecondaryContact(individualContact)
        ) mustBe Json.toJson(individualContact)
      }
    }
  }

  "CreateSubscriptionRequest" - {

    "must serialise and deserialise with both contacts" in {

      val request =
        CreateSubscriptionRequest(
          idType = "UTR",
          idNumber = "1234567890",
          tradingName = Some("Trading Name"),
          gbUser = true,
          primaryContact = PrimaryContact(organisationContact),
          secondaryContact = Some(
            SecondaryContact(individualContact)
          )
        )

      val json = Json.toJson(request)

      json.validate[CreateSubscriptionRequest] mustBe JsSuccess(request)
    }

    "must serialise and deserialise without optional fields" in {

      val request =
        CreateSubscriptionRequest(
          idType = "NINO",
          idNumber = "AB123456C",
          tradingName = None,
          gbUser = false,
          primaryContact = PrimaryContact(individualContact),
          secondaryContact = None
        )

      val json = Json.toJson(request)

      json.validate[CreateSubscriptionRequest] mustBe JsSuccess(request)
    }

    "must fail to deserialise when a required field is missing" in {

      val json = Json.obj(
        "idType" -> "UTR"
      )

      json.validate[CreateSubscriptionRequest].isError mustBe true
    }
  }

}
