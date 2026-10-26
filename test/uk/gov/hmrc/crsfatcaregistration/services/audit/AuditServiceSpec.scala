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

package uk.gov.hmrc.crsfatcaregistration.services.audit

import org.mockito.Mockito.{mockingDetails, reset}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.crsfatcaregistration.models.audit.{CreateRegistration, UpdateContactDetails}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  implicit private val ec: ExecutionContext =
    ExecutionContext.global

  implicit private val hc: HeaderCarrier =
    HeaderCarrier()

  private val mockAuditConnector =
    mock[DefaultAuditConnector]

  private val service =
    new AuditService(mockAuditConnector)

  override def beforeEach(): Unit = {
    reset(mockAuditConnector)
    super.beforeEach()
  }

  "AuditService" - {

    "sendCreateRegistration" - {

      "must send the correct CreateRegistration audit event" in {

        val expectedEvent =
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

        service.sendCreateRegistration(expectedEvent)

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations.iterator().next()

        invocation.getArgument[String](0) mustBe
          "CreateRegistration"

        invocation.getArgument[CreateRegistration](1) mustBe
          expectedEvent
      }

      "must send the audit event when optional address fields are not provided" in {

        val expectedEvent =
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

        service.sendCreateRegistration(expectedEvent)

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations.iterator().next()

        invocation.getArgument[String](0) mustBe
          "CreateRegistration"

        invocation.getArgument[CreateRegistration](1) mustBe
          expectedEvent
      }
    }

    "sendUpdateContactDetails" - {

      "must send the correct UpdateContactDetails audit event" in {

        val expectedEvent =
          UpdateContactDetails(
            fatcaId = "FATCA123456",
            affinityType = "Organisation",
            firstContactName = Some("John Smith"),
            firstContactEmail = Some("john.smith@test.com"),
            firstContactTelephone = Some("441234567890"),
            secondContactName = Some("Jane Smith"),
            secondContactEmail = Some("jane.smith@test.com"),
            secondContactTelephone = Some("441234567891")
          )

        service.sendUpdateContactDetails(expectedEvent)

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations.iterator().next()

        invocation.getArgument[String](0) mustBe
          "UpdateContactDetails"

        invocation.getArgument[UpdateContactDetails](1) mustBe
          expectedEvent
      }

      "must send the audit event when optional contact details are not provided" in {

        val expectedEvent =
          UpdateContactDetails(
            fatcaId = "FATCA123456",
            affinityType = "Individual",
            email = Some("john.smith@test.com"),
            telephone = None
          )

        service.sendUpdateContactDetails(expectedEvent)

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations.iterator().next()

        invocation.getArgument[String](0) mustBe
          "UpdateContactDetails"

        invocation.getArgument[UpdateContactDetails](1) mustBe
          expectedEvent
      }
    }
  }

}
