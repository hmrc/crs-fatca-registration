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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.crsfatcaregistration.models.audit.CreateRegistration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector
import play.api.libs.json.OWrites
import scala.concurrent.ExecutionContext
import org.mockito.Mockito.{mockingDetails, reset}

class AuditServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  implicit private val ec: ExecutionContext =
    ExecutionContext.global

  implicit private val hc: HeaderCarrier =
    HeaderCarrier()

  implicit private val createRegistrationWrites: OWrites[CreateRegistration] =
    CreateRegistration.format

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

        service.sendCreateRegistration(
          affinityType = expectedEvent.affinityType,
          registeringAs = expectedEvent.registeringAs,
          registrationType = expectedEvent.registrationType,
          idType = expectedEvent.idType,
          idValue = expectedEvent.idValue,
          tradingName = expectedEvent.tradingName,
          businessName = expectedEvent.businessName,
          addressLine1 = expectedEvent.addressLine1,
          addressLine2 = expectedEvent.addressLine2,
          city = expectedEvent.city,
          region = expectedEvent.region,
          postcode = expectedEvent.postcode,
          country = expectedEvent.country,
          uprn = expectedEvent.uprn,
          dateOfBirth = expectedEvent.dateOfBirth,
          firstContactName = expectedEvent.firstContactName,
          firstContactEmail = expectedEvent.firstContactEmail,
          firstContactTelephone = expectedEvent.firstContactTelephone,
          secondContactName = expectedEvent.secondContactName,
          secondContactEmail = expectedEvent.secondContactEmail,
          secondContactTelephone = expectedEvent.secondContactTelephone,
          fatcaId = expectedEvent.fatcaId
        )

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations
            .iterator()
            .next()

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

        service.sendCreateRegistration(
          affinityType = expectedEvent.affinityType,
          registeringAs = expectedEvent.registeringAs,
          registrationType = expectedEvent.registrationType,
          idType = expectedEvent.idType,
          idValue = expectedEvent.idValue,
          addressLine1 = None,
          city = None,
          country = None,
          dateOfBirth = expectedEvent.dateOfBirth,
          firstContactName = expectedEvent.firstContactName,
          firstContactEmail = expectedEvent.firstContactEmail,
          firstContactTelephone = expectedEvent.firstContactTelephone,
          fatcaId = expectedEvent.fatcaId
        )

        val invocations =
          mockingDetails(mockAuditConnector).getInvocations

        invocations.size() mustBe 1

        val invocation =
          invocations
            .iterator()
            .next()

        invocation.getArgument[String](0) mustBe
          "CreateRegistration"

        invocation.getArgument[CreateRegistration](1) mustBe
          expectedEvent
      }
    }
  }

}
