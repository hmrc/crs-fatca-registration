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
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.crsfatcaregistration.auth.AdminOnlyAuthAction
import uk.gov.hmrc.crsfatcaregistration.config.AppConfig
import uk.gov.hmrc.crsfatcaregistration.connectors.RegistrationConnector
import uk.gov.hmrc.crsfatcaregistration.models.{ErrorDetails, RegisterWithID, RegisterWithoutId}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class RegistrationController @Inject() (
  val config: AppConfig,
  authenticate: AdminOnlyAuthAction,
  registrationConnector: RegistrationConnector,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents) {

  private val logger: Logger = Logger(this.getClass)

  private def withoutIDRegistration(
    request: Request[JsValue]
  )(implicit hc: HeaderCarrier) = {
    val noIdOrganisationRegistration: JsResult[RegisterWithoutId] =
      request.body.validate[RegisterWithoutId]

    noIdOrganisationRegistration.fold(
      invalid = _ => Future.successful(BadRequest("")),
      valid = sub =>
        for {
          response <- registrationConnector.sendWithoutIDInformation(sub)
        } yield convertToResult(response)
    )
  }

  def withoutID: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      withoutIDRegistration(request)
  }

  def withoutOrgID: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      withoutIDRegistration(request)
  }

  def withUTR: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      withIdRegistration(request)
  }

  def withNino: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      withIdRegistration(request)
  }

  def withOrgUTR: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      withIdRegistration(request)
  }

  private def withIdRegistration(
    request: Request[JsValue]
  )(implicit hc: HeaderCarrier) = {
    val withIDRegistration: JsResult[RegisterWithID] =
      request.body.validate[RegisterWithID]

    withIDRegistration.fold(
      invalid = _ => Future.successful(BadRequest("")),
      valid = sub =>
        for {
          response <- registrationConnector.sendWithID(sub)
        } yield convertToResult(response)
    )
  }

  private def convertToResult(httpResponse: HttpResponse): Result =
    httpResponse.status match {
      case OK        => Ok(httpResponse.body)
      case NOT_FOUND => NotFound(httpResponse.body)

      case BAD_REQUEST =>
        logDownStreamError(httpResponse.body)

        BadRequest(httpResponse.body)

      case FORBIDDEN =>
        logDownStreamError(httpResponse.body)

        Forbidden(httpResponse.body)

      case _ =>
        logDownStreamError(httpResponse.body)
        InternalServerError(httpResponse.body)
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

}
