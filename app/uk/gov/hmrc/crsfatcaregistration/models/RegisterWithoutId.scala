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

import java.time.LocalDate

case class NoIdIndividual(name: Name, dateOfBirth: LocalDate)

object NoIdIndividual {

  implicit lazy val writes: OWrites[NoIdIndividual] = OWrites[NoIdIndividual] {
    individual =>
      Json.obj(
        "firstName"   -> individual.name.firstName,
        "lastName"    -> individual.name.secondName,
        "dateOfBirth" -> individual.dateOfBirth.toString
      )
  }

  implicit lazy val reads: Reads[NoIdIndividual] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "firstName").read[String] and
        (__ \ "lastName").read[String] and
        (__ \ "dateOfBirth").read[LocalDate]
    )(
      (firstName, secondName, dob) => NoIdIndividual(Name(firstName, secondName), dob)
    )
  }

}

case class NoIdOrganisation(organisationName: String)

object NoIdOrganisation {

  implicit val format: OFormat[NoIdOrganisation] = Json.format[NoIdOrganisation]

}

case class Address(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: String,
  addressLine4: Option[String],
  postalCode: Option[String],
  countryCode: String
)

object Address {
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
}

case class ContactDetails(
  phoneNumber: Option[String],
  mobileNumber: Option[String],
  faxNumber: Option[String],
  emailAddress: Option[String]
)

object ContactDetails {
  implicit val contactFormats: OFormat[ContactDetails] = Json.format[ContactDetails]
}

case class Identification(
  idNumber: String,
  issuingInstitution: String,
  issuingCountryCode: String
)

object Identification {
  implicit val indentifierFormats: OFormat[Identification] = Json.format[Identification]
}

case class RequestParameter(paramName: String, paramValue: String)

object RequestParameter {
  implicit val indentifierFormats: OFormat[RequestParameter] = Json.format[RequestParameter]
}

case class RequestDetails(
  organisation: Option[NoIdOrganisation],
  individual: Option[NoIdIndividual],
  address: Address,
  contactDetails: ContactDetails,
  identification: Option[Identification]
)

object RequestDetails {

  implicit lazy val residentWrites: OWrites[RequestDetails] = Json.writes[RequestDetails]

  implicit lazy val reads: Reads[RequestDetails] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "organisation").readNullable[NoIdOrganisation] and
        (__ \ "individual").readNullable[NoIdIndividual] and
        (__ \ "address").read[Address] and
        (__ \ "contactDetails").read[ContactDetails] and
        (__ \ "identification").readNullable[Identification]
    )(
      (organisation, individual, address, contactDetails, identification) =>
        (organisation, individual) match {
          case (None, None) =>
            throw new Exception(
              "Request Details must have either an organisation or individual element"
            )
          case (Some(_), Some(_)) =>
            throw new Exception(
              "Request details cannot have both and organisation or individual element"
            )
          case (organisation, individual) =>
            RequestDetails(
              organisation,
              individual,
              address,
              contactDetails,
              identification
            )
        }
    )
  }

}

case class RegisterWithoutIDRequest(
  requestCommon: RequestCommon,
  requestDetail: RequestDetails
)

object RegisterWithoutIDRequest {
  implicit val format: OFormat[RegisterWithoutIDRequest] = Json.format[RegisterWithoutIDRequest]
}

case class RegisterWithoutId(
  registerWithoutIDRequest: RegisterWithoutIDRequest
)

object RegisterWithoutId {
  implicit val format: OFormat[RegisterWithoutId] = Json.format[RegisterWithoutId]
}
