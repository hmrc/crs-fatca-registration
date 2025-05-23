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

import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.crsfatcaregistration.auth.AuthActionSets
import uk.gov.hmrc.crsfatcaregistration.config.AppConfig
import uk.gov.hmrc.crsfatcaregistration.connectors.SubscriptionConnector
import uk.gov.hmrc.crsfatcaregistration.models.{CreateSubscriptionRequest, DisplaySubscriptionRequest, ErrorDetails, UpdateSubscriptionRequest}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class SubscriptionController @Inject() (
  val config: AppConfig,
  authenticator: AuthActionSets,
  subscriptionConnector: SubscriptionConnector,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents) {

  private val logger: Logger = Logger(this.getClass)

  def readSubscription: Action[JsValue] = authenticator.authenticateAll(parse.json).async {
    implicit request =>
      val subscriptionReadResult: JsResult[DisplaySubscriptionRequest] =
        request.body.validate[DisplaySubscriptionRequest]

      subscriptionReadResult.fold(
        invalid = _ =>
          Future.successful(
            BadRequest("DisplaySubscriptionRequest is invalid")
          ),
        valid = sub =>
          for {
            response <- subscriptionConnector.readSubscriptionInformation(sub)
          } yield convertToResult(response)
      )
  }

  def updateSubscription(): Action[JsValue] = authenticator.authenticateAll(parse.json).async {
    implicit request =>
      request.body
        .validate[UpdateSubscriptionRequest]
        .fold(
          invalid = _ => Future.successful(BadRequest("UpdateSubscriptionRequest is invalid")),
          valid = validRequest => subscriptionConnector.updateSubscriptionInformation(validRequest).map(convertToResult)
        )
  }

  def createSubscription: Action[JsValue] = authenticator.authenticateAdmin(parse.json).async {
    implicit request =>
      val subscriptionSubmissionResult: JsResult[CreateSubscriptionRequest] =
        request.body.validate[CreateSubscriptionRequest]

      subscriptionSubmissionResult.fold(
        invalid = _ =>
          Future.successful(
            BadRequest("CreateSubscriptionRequest is invalid")
          ),
        valid = sub =>
          for {
            response <- subscriptionConnector.sendSubscriptionInformation(sub)
          } yield convertToResult(response)
      )
  }

  private def convertToResult(httpResponse: HttpResponse): Result = {
    logDownStreamError(httpResponse.body)
    httpResponse.status match {
      case status if is2xx(status) => Ok(httpResponse.body)
      case NOT_FOUND               => NotFound(httpResponse.body)
      case BAD_REQUEST             => BadRequest(httpResponse.body)
      case SERVICE_UNAVAILABLE     => ServiceUnavailable(httpResponse.body)
      case FORBIDDEN               => Forbidden(httpResponse.body)
      case UNPROCESSABLE_ENTITY =>
        if (isAlreadyRegistered(httpResponse.body)) {
          logger.warn(s"Already registered. ${httpResponse.status} response status")
          val responseJson = Json.parse(httpResponse.body).asOpt[JsObject].getOrElse(Json.obj())
          UnprocessableEntity(responseJson + ("status" -> Json.toJson("already_registered")))
        } else {
          logger.warn(s"Duplicate submission to ETMP. ${httpResponse.status} response status")
          UnprocessableEntity(httpResponse.body)
        }
      case _ =>
        logger.warn(s"Unexpected error from ETMP. ${httpResponse.status} response status")
        InternalServerError(httpResponse.body)
    }
  }

  private def logDownStreamError(body: String): Unit = {
    val error = Try(Json.parse(body).validate[ErrorDetails])
    error match {
      case Success(JsSuccess(value, _)) =>
        logger.warn(
          s"Error with submission: ${value.errorDetail.sourceFaultDetail.map(_.detail.mkString)}"
        )
      case _ =>
        logger.warn("Error with submission but return is not a valid json")
    }
  }

  private def isAlreadyRegistered(responseBody: String): Boolean =
    Try(Json.parse(responseBody)).toOption
      .flatMap(
        json => (json \\ "errorCode").headOption.flatMap(_.asOpt[String])
      )
      .contains("007")

}
