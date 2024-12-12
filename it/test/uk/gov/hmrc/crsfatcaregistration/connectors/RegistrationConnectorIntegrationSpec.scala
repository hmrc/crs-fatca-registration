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

package it.test.uk.gov.hmrc.crsfatcaregistration.connectors

import it.test.uk.gov.hmrc.crsfatcaregistration.wiremock.WireMockHelper
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.{Application, Configuration}
import play.api.http.Status._
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.connectors.RegistrationConnector
import uk.gov.hmrc.crsfatcaregistration.generators.Generators
import uk.gov.hmrc.crsfatcaregistration.models.{RegisterWithID, RegisterWithoutId}

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationConnectorIntegrationSpec extends SpecBase with IntegrationPatience with WireMockHelper with Generators with ScalaCheckPropertyChecks {

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMock()
  }

  override lazy val app: Application = applicationBuilder()
    .configure(
      Configuration(
        "microservice.services.register-with-id.port"    -> wireMockServer.port(),
        "microservice.services.register-without-id.port" -> wireMockServer.port()
      )
    )
    .build()

  lazy val connector: RegistrationConnector =
    app.injector.instanceOf[RegistrationConnector]

  "RegistrationConnector" - {
    "for a registration without id submission" - {
      "must return status as OK for submission of Subscription" in {

        forAll(arbitrary[RegisterWithoutId]) {
          sub =>
            stubResponse("/dac6/dct102a/v1", OK)

            val result = connector.sendWithoutIDInformation(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return status as BAD_REQUEST for submission of invalid subscription" in {

        forAll(arbitrary[RegisterWithoutId]) {
          sub =>
            stubResponse("/dac6/dct102a/v1", BAD_REQUEST)

            val result = connector.sendWithoutIDInformation(sub)
            result.futureValue.status mustBe BAD_REQUEST
        }
      }

      "must return status as INTERNAL_SERVER_ERROR for submission for a technical error" in {

        forAll(arbitrary[RegisterWithoutId]) {
          sub =>
            stubResponse("/dac6/dct102a/v1", INTERNAL_SERVER_ERROR)

            val result = connector.sendWithoutIDInformation(sub)
            result.futureValue.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "for a registration with id submission" - {
      "must return status as OK for submission of Subscription" in {

        forAll(arbitrary[RegisterWithID]) {
          sub =>
            stubResponse("/dac6/dct102b/v1", OK)

            val result = connector.sendWithID(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return status as BAD_REQUEST for submission of invalid subscription" in {

        forAll(arbitrary[RegisterWithID]) {
          sub =>
            stubResponse("/dac6/dct102b/v1", BAD_REQUEST)

            val result = connector.sendWithID(sub)
            result.futureValue.status mustBe BAD_REQUEST
        }
      }

      "must return status as INTERNAL_SERVER_ERROR for submission for a technical error" in {

        forAll(arbitrary[RegisterWithID]) {
          sub =>
            stubResponse("/dac6/dct102b/v1", INTERNAL_SERVER_ERROR)

            val result = connector.sendWithID(sub)
            result.futureValue.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

}
