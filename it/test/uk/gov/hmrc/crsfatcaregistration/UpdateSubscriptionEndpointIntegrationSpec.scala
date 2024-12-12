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

package it.test.uk.gov.hmrc.crsfatcaregistration

import com.github.tomakehurst.wiremock.http.RequestMethod
import it.test.uk.gov.hmrc.crsfatcaregistration.wiremock.WireMockHelper
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.auth.{AllowAllAuthAction, FakeAllowAllAuthAction}
import uk.gov.hmrc.crsfatcaregistration.generators.{Generators, ModelGenerators}
import uk.gov.hmrc.crsfatcaregistration.models.UpdateSubscriptionRequest

class UpdateSubscriptionEndpointIntegrationSpec
    extends SpecBase
    with GuiceOneServerPerSuite
    with ModelGenerators
    with Generators
    with IntegrationPatience
    with WireMockHelper
    with TableDrivenPropertyChecks {

  startWireMock()

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMock()
  }

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      Configuration(
        "metrics.enabled"                                -> false,
        "auditing.enabled"                               -> false,
        "microservice.services.update-subscription.port" -> wireMockServer.port()
      )
    )
    .overrides(bind[AllowAllAuthAction].to[FakeAllowAllAuthAction])
    .build()

  override lazy val app: Application = fakeApplication()

  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl  = s"http://localhost:$port"

  private val apiErrorCodes = Table(
    ("apiErrorCodes", "expectedErrorCode"),
    (NOT_FOUND, NOT_FOUND),
    (BAD_REQUEST, BAD_REQUEST),
    (FORBIDDEN, FORBIDDEN),
    (UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY),
    (BAD_GATEWAY, INTERNAL_SERVER_ERROR),
    (SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE),
  )

  "update subscription endpoint" - {

    val UpdateSubscriptionUrl = "/dac6/dct70e/v1"

    "should respond with OK status when subscription update succeeds" in {

      stubResponse(UpdateSubscriptionUrl, OK, RequestMethod.PUT)

      ScalaCheckPropertyChecks.forAll(arbitrary[UpdateSubscriptionRequest]) {
        updateSubscriptionRequest =>
          val response =
            wsClient
              .url(s"$baseUrl/crs-fatca-registration/subscription/update-subscription")
              .put(Json.toJson(updateSubscriptionRequest))
              .futureValue

          response.status shouldBe OK
      }
    }

    forAll(apiErrorCodes) {
      (apiErrorCode, expectedErrorCode) =>
        s"should respond with [$expectedErrorCode] status when subscription API returns [$apiErrorCode]" in {

          stubResponse(UpdateSubscriptionUrl, apiErrorCode, RequestMethod.PUT)

          ScalaCheckPropertyChecks.forAll(arbitrary[UpdateSubscriptionRequest]) {
            updateSubscriptionRequest =>
              val response =
                wsClient
                  .url(s"$baseUrl/crs-fatca-registration/subscription/update-subscription")
                  .put(Json.toJson(updateSubscriptionRequest))
                  .futureValue

              response.status shouldBe expectedErrorCode
          }
        }
    }
  }

}
