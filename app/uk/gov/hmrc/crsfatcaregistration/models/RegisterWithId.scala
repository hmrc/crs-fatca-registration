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

package uk.gov.hmrc.crsfatcaregistration.models

import play.api.libs.json._

case class RegisterWithID(registerWithIDRequest: RegisterWithIDRequest)

object RegisterWithID {

  implicit val format: Format[RegisterWithID] =
    Json.format[RegisterWithID]

}

case class RegisterWithIDRequest(
  requestCommon: RequestCommon,
  requestDetail: RequestWithIDDetails
)

object RegisterWithIDRequest {

  implicit val format: Format[RegisterWithIDRequest] =
    Json.format[RegisterWithIDRequest]

}

sealed trait PartnerDetails

case class WithIDIndividual(
  firstName: String,
  middleName: Option[String],
  lastName: String,
  dateOfBirth: Option[String]
) extends PartnerDetails

object WithIDIndividual {
  implicit val format: OFormat[WithIDIndividual] = Json.format[WithIDIndividual]
}

case class WithIDOrganisation(
  organisationName: String,
  organisationType: String
) extends PartnerDetails

object WithIDOrganisation {
  implicit val format: OFormat[WithIDOrganisation] = Json.format[WithIDOrganisation]
}

case class RequestWithIDDetails(
  IDType: String,
  IDNumber: String,
  requiresNameMatch: Boolean,
  isAnAgent: Boolean,
  partnerDetails: Option[PartnerDetails] = None
)

object RequestWithIDDetails {

  implicit lazy val requestWithIDDetailsReads: Reads[RequestWithIDDetails] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
        (__ \ "IDNumber").read[String] and
        (__ \ "requiresNameMatch").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "individual").readNullable[WithIDIndividual] and
        (__ \ "organisation").readNullable[WithIDOrganisation]
    )(
      (
        idType,
        idNumber,
        requiresNameMatch,
        isAnAgent,
        individual,
        organisation
      ) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) =>
            throw new Exception(
              "Request details cannot have both and organisation or individual element"
            )
          case (Some(ind), _) =>
            RequestWithIDDetails(
              idType,
              idNumber,
              requiresNameMatch,
              isAnAgent,
              Option(ind)
            )
          case (_, Some(org)) =>
            RequestWithIDDetails(
              idType,
              idNumber,
              requiresNameMatch,
              isAnAgent,
              Option(org)
            )
          case (None, None) =>
            RequestWithIDDetails(
              idType,
              idNumber,
              requiresNameMatch,
              isAnAgent,
              partnerDetails = None
            )
        }
    )
  }

  implicit lazy val requestWithIDDetailsWrites: OWrites[RequestWithIDDetails] =
    OWrites[RequestWithIDDetails] {
      case RequestWithIDDetails(
            idType,
            idNumber,
            requiresNameMatch,
            isAnAgent,
            Some(individual @ WithIDIndividual(_, _, _, _))
          ) =>
        Json.obj(
          "IDType"            -> idType,
          "IDNumber"          -> idNumber,
          "requiresNameMatch" -> requiresNameMatch,
          "isAnAgent"         -> isAnAgent,
          "individual"        -> individual
        )
      case RequestWithIDDetails(
            idType,
            idNumber,
            requiresNameMatch,
            isAnAgent,
            Some(organisation @ WithIDOrganisation(_, _))
          ) =>
        Json.obj(
          "IDType"            -> idType,
          "IDNumber"          -> idNumber,
          "requiresNameMatch" -> requiresNameMatch,
          "isAnAgent"         -> isAnAgent,
          "organisation"      -> organisation
        )
      case RequestWithIDDetails(
            idType,
            idNumber,
            requiresNameMatch,
            isAnAgent,
            None
          ) =>
        Json.obj(
          "IDType"            -> idType,
          "IDNumber"          -> idNumber,
          "requiresNameMatch" -> requiresNameMatch,
          "isAnAgent"         -> isAnAgent
        )
    }

}
