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
import uk.gov.hmrc.crsfatcaregistration.models.{
  ErrorDetails,
  RegisterWithID,
  RegisterWithIDRequest,
  RegisterWithoutId,
  RequestCommon,
  RequestWithIDDetails,
  UniqueTaxpayerReference
}
import uk.gov.hmrc.crsfatcaregistration.services.audit.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class RegistrationController @Inject() (
  val config: AppConfig,
  authenticate: AdminOnlyAuthAction,
  registrationConnector: RegistrationConnector,
  auditService: AuditService,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents) {

  private val logger: Logger = Logger(this.getClass)

  private def withoutIDRegistration(
    request: Request[JsValue]
  )(implicit hc: HeaderCarrier): Future[Result] =
    request.body
      .validate[RegisterWithoutId]
      .fold(
        invalid = _ => Future.successful(BadRequest("")),
        valid = sub =>
          registrationConnector
            .sendWithoutIDInformation(sub)
            .map {
              response =>
                val result = convertToResult(response)

                if (isSuccessful(result)) {
                  val detail =
                    sub.registerWithoutIDRequest.requestDetail

                  val address =
                    detail.address

                  val contactDetails =
                    detail.contactDetails

                  val isOrganisation =
                    detail.organisation.isDefined

                  auditService.sendCreateRegistration(
                    affinityType = if (isOrganisation) "Organisation" else "Individual",
                    registeringAs = if (isOrganisation) "Organisation" else "Individual",
                    registrationType = if (isOrganisation) "OrgWithoutID" else "IndividualWithoutId",
                    idType = if (isOrganisation) "NotProvided" else "NINO",
                    idValue = detail.identification
                      .map(_.idNumber)
                      .getOrElse("NoIdProvided"),
                    tradingName = None,
                    businessName = detail.organisation
                      .map(_.organisationName),
                    addressLine1 = address.addressLine1,
                    addressLine2 = address.addressLine2,
                    city = address.addressLine3,
                    region = address.addressLine4,
                    postcode = address.postalCode,
                    country = address.countryCode,
                    uprn = None,
                    dateOfBirth = detail.individual
                      .map(_.dateOfBirth.toString),
                    firstContactName = detail.individual
                      .map {
                        individual =>
                          s"${individual.name.firstName} ${individual.name.secondName}"
                      }
                      .getOrElse("/"),
                    firstContactEmail = contactDetails.emailAddress
                      .getOrElse("/"),
                    firstContactTelephone = contactDetails.phoneNumber
                      .orElse(contactDetails.mobileNumber),
                    secondContactName = None,
                    secondContactEmail = None,
                    secondContactTelephone = None,
                    fatcaId = "/"
                  )
                }

                result
            }
      )

  private def isSuccessful(result: Result): Boolean =
    result.header.status >= 200 && result.header.status < 300

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

  def withUTROnly: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      val uniqueTaxReference: JsResult[UniqueTaxpayerReference] =
        request.body.validate[UniqueTaxpayerReference]

      uniqueTaxReference.fold(
        invalid = _ => Future.successful(BadRequest("")),
        valid = utr =>
          for {
            response <- registrationConnector.sendWithID(buildPayload(utr))
          } yield convertToResult(response)
      )

  }

  private def buildPayload(utr: UniqueTaxpayerReference): RegisterWithID =
    RegisterWithID(
      RegisterWithIDRequest(
        requestCommon = RequestCommon.apply,
        requestDetail = RequestWithIDDetails(
          "UTR",
          utr.value,
          requiresNameMatch = false,
          isAnAgent = false,
          partnerDetails = None
        )
      )
    )

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
