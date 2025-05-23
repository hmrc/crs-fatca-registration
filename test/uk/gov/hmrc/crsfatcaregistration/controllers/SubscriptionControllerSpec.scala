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

package uk.gov.hmrc.crsfatcaregistration.controllers

import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.auth._
import uk.gov.hmrc.crsfatcaregistration.config.AppConfig
import uk.gov.hmrc.crsfatcaregistration.connectors.SubscriptionConnector
import uk.gov.hmrc.crsfatcaregistration.generators.Generators
import uk.gov.hmrc.crsfatcaregistration.models._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val connectorErrorCodes = Table(
    ("connectorErrorCodes", "expectedErrorCode"),
    (NOT_FOUND, NOT_FOUND),
    (BAD_REQUEST, BAD_REQUEST),
    (FORBIDDEN, FORBIDDEN),
    (UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY),
    (BAD_GATEWAY, INTERNAL_SERVER_ERROR),
    (SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
  )

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val mockSubscriptionConnector: SubscriptionConnector =
    mock[SubscriptionConnector]

  val application: Application = applicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[AdminOnlyAuthAction].to[FakeAdminOnlyAuthAction],
      bind[AllowAllAuthAction].to[FakeAllowAllAuthAction]
    )
    .build()

  "SubscriptionController" - {

    "sendSubscription" - {
      "should return UnprocessableEntity with a status of already_registered when EIS returns 422 with errorCode 007" in {

        val eisResponseJson = Json.obj(
          "errorCode" -> "007"
        )

        val expectedResponseJson = eisResponseJson + ("status" -> Json.toJson("already_registered"))

        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                422,
                eisResponseJson.toString(),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              ).withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value

            status(result) mustEqual UNPROCESSABLE_ENTITY
            contentAsJson(result) mustBe expectedResponseJson
        }
      }

      "should return BAD_REQUEST when subscriptionRequest ia invalid" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        val request =
          FakeRequest(
            POST,
            routes.SubscriptionController.createSubscription.url
          )
            .withJsonBody(Json.parse("""{"value": "field"}"""))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }

      "should return OK when subscriptionRequest is valid" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        ).thenReturn(
          Future.successful(
            HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual OK
        }
      }

      "should return BAD_REQUEST when one is encountered" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST
        }
      }

      "should return FORBIDDEN when authorisation is invalid" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(403, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual FORBIDDEN
        }
      }

      "should return SERVICE_UNAVAILABLE when EIS becomes unavailable" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }

      "should return INTERNAL_SERVER_ERROR when EIS fails" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                BAD_GATEWAY,
                Json.obj(),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "should return UNPROCESSABLE_ENTITY when one occurs" in {
        val errorDetails = ErrorDetails(
          ErrorDetail(
            LocalDate.now().toString,
            Some("xx"),
            "422",
            "UNPROCESSABLE_ENTITY",
            "",
            Some(SourceFaultDetail(Seq("a", "b")))
          )
        )
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                422,
                Json.toJson(errorDetails),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual UNPROCESSABLE_ENTITY
        }
      }

      "should return NOT_FOUND for unspecified errors" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual NOT_FOUND
        }
      }

      "downstream errors should be recoverable when not in json" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(any[CreateSubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[CreateSubscriptionRequest]) {
          subscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.createSubscription.url
              )
                .withJsonBody(Json.toJson(subscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }

    }

    "readSubscription" - {
      "should return OK when ReadSubscriptionRequest is valid" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        ).thenReturn(
          Future.successful(
            HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual OK
        }
      }

      s"should return OK when ReadSubscriptionRequest is valid and returns $CREATED" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        ).thenReturn(
          Future.successful(
            HttpResponse(201, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual OK
        }
      }

      "should return BAD_REQUEST when DisplaySubscriptionRequest is invalid" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        val request =
          FakeRequest(
            POST,
            routes.SubscriptionController.readSubscription.url
          )
            .withJsonBody(Json.parse("""{"value": "field"}"""))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }

      "should return FORBIDDEN when authorisation is invalid for read Subscription" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(403, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual FORBIDDEN
        }
      }

      "should return SERVICE_UNAVAILABLE when EIS becomes unavailable for read Subscription" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }

      "should return INTERNAL_SERVER_ERROR when EIS fails for readSubscription" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                BAD_GATEWAY,
                Json.obj(),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "should return NOT_FOUND for unspecified errors for read Subscription" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual NOT_FOUND
        }
      }

      "downstream errors should be recoverable when not in json for read subscription" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(any[DisplaySubscriptionRequest]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionRequest]) {
          readSubscriptionRequest =>
            val request =
              FakeRequest(
                POST,
                routes.SubscriptionController.readSubscription.url
              )
                .withJsonBody(Json.toJson(readSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }
    }

    "updateSubscription" - {
      "must respond with OK when connector returns 200" in {
        forAll(arbitrary[UpdateSubscriptionRequest]) {
          updateSubscriptionRequest =>
            when(mockSubscriptionConnector.updateSubscriptionInformation(mockitoEq(updateSubscriptionRequest))(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(HttpResponse(OK, "Some Response", Map.empty)))

            val request = FakeRequest(PUT, routes.SubscriptionController.updateSubscription.url)
              .withJsonBody(Json.toJson(updateSubscriptionRequest))

            val result = route(application, request).value
            status(result) mustEqual OK
        }
      }

      "must respond with BAD_REQUEST when given an invalid request" in {
        val request = FakeRequest(PUT, routes.SubscriptionController.updateSubscription.url).withJsonBody(Json.parse("{}"))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }

      forAll(connectorErrorCodes) {
        (connectorErrorCode, expectedErrorCode) =>
          s"must respond with [$expectedErrorCode] when connector returns [$connectorErrorCode]" in {
            forAll(arbitrary[UpdateSubscriptionRequest]) {
              updateSubscriptionRequest =>
                when(mockSubscriptionConnector.updateSubscriptionInformation(any[UpdateSubscriptionRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
                  .thenReturn(Future.successful(HttpResponse(connectorErrorCode, "Some Error", Map.empty)))

                val request = FakeRequest(PUT, routes.SubscriptionController.updateSubscription.url)
                  .withJsonBody(Json.toJson(updateSubscriptionRequest))

                val result = route(application, request).value
                status(result) mustEqual expectedErrorCode
            }
          }
      }
    }

    "isAlreadyRegistered" - {
      val sut                        = new SubscriptionController(mock[AppConfig], mock[AuthActionSets], mockSubscriptionConnector, mock[ControllerComponents])
      val privateIsAlreadyRegistered = PrivateMethod[Boolean](Symbol("isAlreadyRegistered"))

      def isAlreadyRegistered(json: String) =
        sut.invokePrivate(privateIsAlreadyRegistered(json))

      "return true for a valid JSON containing errorCode 007" in {
        val json =
          """{
            |  "errorDetail": {
            |    "errorCode": "007",
            |    "errorMessage": "Business Partner already has a Subscription for this regime"
            |  }
            |}""".stripMargin

        isAlreadyRegistered(json) mustBe true
      }

      "return false for non valid json" in {
        val invalidPayloads = Seq(
          """{ "invalid": "data" }""",
          "{}"
        )
        for (json <- invalidPayloads)
          isAlreadyRegistered(json) mustBe false
      }
      "return false for valid json without errorCode 007" in {
        val payloads = Seq(
          """{
            |  "errorDetail": {
            |    "errorCode": "008",
            |    "errorMessage": "Another error message"
            |  }
            |}""".stripMargin,
          """{
            |  "errorDetail": {
            |    "errorMessage": "No error code"
            |  }
            |}""".stripMargin
        )
        for (json <- payloads)
          isAlreadyRegistered(json) mustBe false
      }
    }

  }

}
