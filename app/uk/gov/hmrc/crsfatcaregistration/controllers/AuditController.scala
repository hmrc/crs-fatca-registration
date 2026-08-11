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

package uk.gov.hmrc.crsfatcaregistration.controllers

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.crsfatcaregistration.auth.AdminOnlyAuthAction
import uk.gov.hmrc.crsfatcaregistration.models.audit.CreateRegistration
import uk.gov.hmrc.crsfatcaregistration.services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AuditController @Inject() (
  authenticate: AdminOnlyAuthAction,
  auditService: AuditService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents) {

  private val logger: Logger = Logger(this.getClass)

  def createRegistration: Action[JsValue] =
    authenticate(parse.json).async {
      implicit request =>
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

        request.body
          .validate[CreateRegistration]
          .fold(
            invalid = errors => {
              logger.warn(
                s"Unable to validate CreateRegistration audit request: $errors"
              )

              Future.successful(BadRequest(""))
            },
            valid = auditRequest => {
              auditService.sendCreateRegistration(
                affinityType = auditRequest.affinityType,
                registeringAs = auditRequest.registeringAs,
                registrationType = auditRequest.registrationType,
                idType = auditRequest.idType,
                idValue = auditRequest.idValue,
                tradingName = auditRequest.tradingName,
                businessName = auditRequest.businessName,
                addressLine1 = auditRequest.addressLine1,
                addressLine2 = auditRequest.addressLine2,
                city = auditRequest.city,
                region = auditRequest.region,
                postcode = auditRequest.postcode,
                country = auditRequest.country,
                uprn = auditRequest.uprn,
                dateOfBirth = auditRequest.dateOfBirth,
                firstContactName = auditRequest.firstContactName,
                firstContactEmail = auditRequest.firstContactEmail,
                firstContactTelephone = auditRequest.firstContactTelephone,
                secondContactName = auditRequest.secondContactName,
                secondContactEmail = auditRequest.secondContactEmail,
                secondContactTelephone = auditRequest.secondContactTelephone,
                fatcaId = auditRequest.fatcaId
              )

              Future.successful(NoContent)
            }
          )
    }

}
