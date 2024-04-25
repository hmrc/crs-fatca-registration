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

package it.test.uk.gov.hmrc.crsfatcaregistration.connectors

import com.github.tomakehurst.wiremock.http.RequestMethod
import it.test.uk.gov.hmrc.crsfatcaregistration.wiremock.WireMockHelper
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.{Application, Configuration}
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.connectors.SubscriptionConnector
import uk.gov.hmrc.crsfatcaregistration.generators.{Generators, ModelGenerators}
import uk.gov.hmrc.crsfatcaregistration.models.{CreateSubscriptionRequest, DisplaySubscriptionRequest, UpdateSubscriptionRequest}

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorIntegrationSpec
    extends SpecBase
    with ModelGenerators
    with Generators
    with IntegrationPatience
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  val Min4xxErrorCode = 400
  val Max5xxErrorCode = 599

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
        "metrics.enabled"                                -> false,
        "auditing.enabled"                               -> false,
        "microservice.services.create-subscription.port" -> wireMockServer.port(),
        "microservice.services.read-subscription.port"   -> wireMockServer.port(),
        "microservice.services.update-subscription.port" -> wireMockServer.port()
      )
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  private val errorCodes: Gen[Int] = Gen.chooseNum(Min4xxErrorCode, Max5xxErrorCode)

  "SubscriptionConnector" - {
    "create subscription" - {
      "must return status as OK for submission of Subscription" in {
        stubResponse("/dac6/dct70c/v1", OK)

        forAll(arbitrary[CreateSubscriptionRequest]) {
          sub =>
            val result = connector.sendSubscriptionInformation(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return an error status for submission of invalid subscription Data" in {

        forAll(arbitrary[CreateSubscriptionRequest], errorCodes) {
          (sub, errorCode) =>
            stubResponse("/dac6/dct70c/v1", errorCode)

            val result = connector.sendSubscriptionInformation(sub)
            result.futureValue.status mustBe errorCode
        }
      }
    }

    "read subscription" - {
      "must return status as OK for read Subscription" in {

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          sub =>
            stubResponse(s"/dac6/dct70d/v1/${sub.idType}/${sub.idNumber}", OK, RequestMethod.GET)
            val result = connector.readSubscriptionInformation(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return an error status for  invalid read Subscription" in {

        forAll(arbitrary[DisplaySubscriptionRequest], errorCodes) {
          (sub, errorCode) =>
            stubResponse(s"/dac6/dct70d/v1/${sub.idType}/${sub.idNumber}", errorCode, RequestMethod.GET)

            val result = connector.readSubscriptionInformation(sub)
            result.futureValue.status mustBe errorCode
        }
      }
    }

    "update subscription" - {
      val UpdateSubscriptionUrl = "/dac6/dct70e/v1"

      "must return status as OK when the subscription update was successful" in {
        stubResponse(UpdateSubscriptionUrl, OK, RequestMethod.PUT)

        forAll(arbitrary[UpdateSubscriptionRequest]) {
          subscriptionRequest =>
            val result = connector.updateSubscriptionInformation(subscriptionRequest)
            result.futureValue.status mustBe OK
        }
      }

      "must return correct error status when the subscription update fails" in {
        forAll(arbitrary[UpdateSubscriptionRequest], errorCodes) {
          (subscriptionRequest, errorCode) =>
            stubResponse(UpdateSubscriptionUrl, errorCode, RequestMethod.PUT)

            val result = connector.updateSubscriptionInformation(subscriptionRequest)
            result.futureValue.status mustBe errorCode
        }
      }
    }
  }

}
