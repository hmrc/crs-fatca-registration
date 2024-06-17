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

package uk.gov.hmrc.crsfatcaregistration.controllers

import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crsfatcaregistration.SpecBase
import uk.gov.hmrc.crsfatcaregistration.auth.{AdminOnlyAuthAction, FakeAdminOnlyAuthAction}
import uk.gov.hmrc.crsfatcaregistration.connectors.RegistrationWithUtrConnector
import uk.gov.hmrc.crsfatcaregistration.generators.Generators
import uk.gov.hmrc.crsfatcaregistration.models._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithUtrControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {
  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val mockRegistrationConnector: RegistrationWithUtrConnector =
    mock[RegistrationWithUtrConnector]

  val application: Application = applicationBuilder()
    .configure(
      Configuration("metrics.enabled" -> "false", "auditing.enabled" -> false)
    )
    .overrides(
      bind[RegistrationWithUtrConnector].toInstance(mockRegistrationConnector),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[AdminOnlyAuthAction].to[FakeAdminOnlyAuthAction]
    )
    .build()

  private val sutRoute =
    s"${routes.RegistrationWithUtrController.sendAndRetrieveRegWithUtr.url}"

  "for a user with id" - {
    "should send data and return ok" in {
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

      forAll(arbitrary[UniqueTaxpayerReference]) {
        uniqueTaxReference =>
          val request =
            FakeRequest(
              POST,
              sutRoute
            )
              .withJsonBody(Json.toJson(uniqueTaxReference))

          val result = route(application, request).value
          status(result) mustEqual OK
      }
    }

    "should return bad request when BAD_REQUEST is encountered" in {
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

      forAll(arbitrary[UniqueTaxpayerReference]) {
        uniqueTaxReference =>
          val request =
            FakeRequest(
              POST,
              sutRoute
            )
              .withJsonBody(Json.toJson(uniqueTaxReference))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
      }
    }

    "should return bad request when Json cannot be validated" in {
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

      val request =
        FakeRequest(
          POST,
          sutRoute
        )
          .withJsonBody(Json.parse("""{"utr": {"value": ""}}"""))

      val result = route(application, request).value
      status(result) mustEqual BAD_REQUEST

    }

    "should return not found when one is encountered" in {
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
          )
        )

      forAll(arbitrary[UniqueTaxpayerReference]) {
        uniqueTaxReference =>
          val request =
            FakeRequest(
              POST,
              sutRoute
            )
              .withJsonBody(Json.toJson(uniqueTaxReference))

          val result = route(application, request).value
          status(result) mustEqual NOT_FOUND
      }
    }

    "should return forbidden error when authorisation is invalid" in {
      val errorDetails = ErrorDetails(
        ErrorDetail(
          LocalDate.now().toString,
          Some("xx"),
          "403",
          "FORBIDDEN",
          "",
          Some(SourceFaultDetail(Seq("a", "b")))
        )
      )
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(
              403,
              Json.toJson(errorDetails),
              Map.empty[String, Seq[String]]
            )
          )
        )

      forAll(arbitrary[UniqueTaxpayerReference]) {
        uniqueTaxReference =>
          val request =
            FakeRequest(
              POST,
              sutRoute
            )
              .withJsonBody(Json.toJson(uniqueTaxReference))

          val result = route(application, request).value
          status(result) mustEqual FORBIDDEN
      }
    }

    "downstream errors should be recoverable when not in json" in {
      when(
        mockRegistrationConnector.sendAndRetrieveRegWithUtr(any[RegisterWithID]())(
          any[HeaderCarrier](),
          any[ExecutionContext]()
        )
      )
        .thenReturn(
          Future.successful(
            HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
          )
        )

      forAll(arbitrary[UniqueTaxpayerReference]) {
        uniqueTaxReference =>
          val request =
            FakeRequest(
              POST,
              sutRoute
            )
              .withJsonBody(Json.toJson(uniqueTaxReference))

          val result = route(application, request).value
          status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

  }

}
