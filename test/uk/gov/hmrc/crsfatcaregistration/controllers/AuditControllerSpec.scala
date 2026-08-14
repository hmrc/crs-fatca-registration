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

package uk.gov.hmrc.crsfatcaregistration.controllers

import org.mockito.Mockito.verifyNoInteractions
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.auth.{AdminOnlyAuthAction, FakeAdminOnlyAuthAction}
import uk.gov.hmrc.crsfatcaregistration.models.audit.CreateRegistration
import uk.gov.hmrc.crsfatcaregistration.services.audit.AuditService

class AuditControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockAuthConnector: AuthConnector =
    mock[AuthConnector]

  private val mockAuditService: AuditService =
    mock[AuditService]

  private val application: Application =
    applicationBuilder()
      .configure(
        Configuration(
          "metrics.enabled"  -> "false",
          "auditing.enabled" -> false
        )
      )
      .overrides(
        bind[AuthConnector]
          .toInstance(mockAuthConnector),
        bind[AdminOnlyAuthAction]
          .to[FakeAdminOnlyAuthAction],
        bind[AuditService]
          .toInstance(mockAuditService)
      )
      .build()

  override def beforeEach(): Unit = {
    reset(mockAuditService)
    super.beforeEach()
  }

  private val auditRoute =
    routes.AuditController.createRegistration.url

  private val fullAuditRequest =
    CreateRegistration(
      affinityType = "Organisation",
      registeringAs = "Organisation",
      registrationType = "OrgWithoutID",
      idType = "NotProvided",
      idValue = "NotProvided",
      tradingName = Some("Test Trading Name"),
      businessName = Some("Test Business"),
      addressLine1 = Some("1 Test Street"),
      addressLine2 = Some("Test Area"),
      city = Some("London"),
      region = Some("Greater London"),
      postcode = Some("AA1 1AA"),
      country = Some("GB"),
      uprn = None,
      dateOfBirth = None,
      firstContactName = "John Smith",
      firstContactEmail = "john.smith@test.com",
      firstContactTelephone = Some("441234567890"),
      secondContactName = Some("Jane Smith"),
      secondContactEmail = Some("jane.smith@test.com"),
      secondContactTelephone = Some("441234567891"),
      fatcaId = "FATCA123456"
    )

  private val individualWithIdAuditRequest =
    CreateRegistration(
      affinityType = "Individual",
      registeringAs = "Individual",
      registrationType = "IndividualWithID",
      idType = "NINO",
      idValue = "CC123456C",
      tradingName = None,
      businessName = None,
      addressLine1 = None,
      addressLine2 = None,
      city = None,
      region = None,
      postcode = None,
      country = None,
      uprn = None,
      dateOfBirth = Some("1996-03-08"),
      firstContactName = "John Smith",
      firstContactEmail = "john.smith@test.com",
      firstContactTelephone = Some("441234567890"),
      secondContactName = None,
      secondContactEmail = None,
      secondContactTelephone = None,
      fatcaId = "FATCA123456"
    )

  private def verifyAuditServiceCalled(): Unit = {

    val invocations =
      mockingDetails(mockAuditService).getInvocations

    invocations.size() mustBe 1

    invocations
      .iterator()
      .next()
      .getMethod
      .getName mustBe "sendCreateRegistration"
  }

  "AuditController" - {

    "createRegistration" - {

      "must send the audit request and return NO_CONTENT" in {

        val request =
          FakeRequest(
            POST,
            auditRoute
          ).withJsonBody(
            Json.toJson(fullAuditRequest)
          )

        val result =
          route(application, request).value

        status(result) mustBe NO_CONTENT

        verifyAuditServiceCalled()
      }

      "must accept an audit request without address information" in {

        val request =
          FakeRequest(
            POST,
            auditRoute
          ).withJsonBody(
            Json.toJson(individualWithIdAuditRequest)
          )

        val result =
          route(application, request).value

        status(result) mustBe NO_CONTENT

        verifyAuditServiceCalled()
      }

      "must return BAD_REQUEST when Json cannot be validated" in {

        val request =
          FakeRequest(
            POST,
            auditRoute
          ).withJsonBody(
            Json.obj(
              "affinityType"  -> "Individual",
              "registeringAs" -> "Individual"
            )
          )

        val result =
          route(application, request).value

        status(result) mustBe BAD_REQUEST

        verifyNoInteractions(mockAuditService)
      }
    }
  }

}
