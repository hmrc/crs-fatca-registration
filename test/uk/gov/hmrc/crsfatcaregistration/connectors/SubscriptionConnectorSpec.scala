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

package uk.gov.hmrc.crsfatcaregistration.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, request, urlEqualTo}
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import uk.gov.hmrc.crsfatcaregistration.base.{SpecBase, WireMockServerHandler}
import uk.gov.hmrc.crsfatcaregistration.generators.Generators
import uk.gov.hmrc.crsfatcaregistration.models.{CreateSubscriptionRequest, ReadSubscriptionRequestDetail}

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {
  val minT = 400
  val maxT = 599

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.create-subscription.port" -> server.port(),
      "microservice.services.read-subscription.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector =
    app.injector.instanceOf[SubscriptionConnector]

  private val errorCodes: Gen[Int] = Gen.chooseNum(minT, maxT)

  "SubscriptionConnector" - {
    "create subscription" - {
      "must return status as OK for submission of Subscription" in {
        stubResponse(
          RequestMethod.POST,
          "/dac6/dct70c/v1",
          OK
        )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          sub =>
            val result = connector.sendSubscriptionInformation(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return an error status for submission of invalid subscription Data" in {

        forAll(arbitrary[CreateSubscriptionRequest], errorCodes) {
          (sub, errorCode) =>
            stubResponse(
              RequestMethod.POST,
              "/dac6/dct70c/v1",
              errorCode
            )

            val result = connector.sendSubscriptionInformation(sub)
            result.futureValue.status mustBe errorCode
        }
      }
    }

    "read subscription" - {
      "must return status as OK for read Subscription" in {
        forAll(arbitrary[ReadSubscriptionRequestDetail]) {
          sub =>
            stubResponse(
              RequestMethod.GET,
              s"/dac6/dct70d/v1/${sub.idType}/${sub.idNumber}",
              OK
            )

            val result = connector.readSubscriptionInformation(sub)
            result.futureValue.status mustBe OK
        }
      }

      "must return an error status for  invalid read Subscription" in {
        forAll(arbitrary[ReadSubscriptionRequestDetail], errorCodes) {
          (sub, errorCode) =>
            stubResponse(
              RequestMethod.GET,
              s"/dac6/dct70d/v1/${sub.idType}/${sub.idNumber}",
              errorCode
            )

            val result = connector.readSubscriptionInformation(sub)
            result.futureValue.status mustBe errorCode
        }
      }
    }
  }

  private def stubResponse(
    requestMethod: RequestMethod,
    expectedUrl: String,
    expectedStatus: Int
  ): StubMapping =
    server.stubFor(
      request(requestMethod.getName, urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )

}
