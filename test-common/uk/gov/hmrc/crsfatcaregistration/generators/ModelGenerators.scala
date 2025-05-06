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

package uk.gov.hmrc.crsfatcaregistration.generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.crsfatcaregistration.models._
import uk.gov.hmrc.domain.Nino
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate

trait ModelGenerators {
  self: Generators =>
  val stringMaxLen = 32

  implicit val arbitraryName: Arbitrary[Name] = Arbitrary {
    val nameMaxLen = 50

    for {
      firstName  <- stringsWithMaxLength(nameMaxLen)
      secondName <- stringsWithMaxLength(nameMaxLen)
    } yield Name(firstName, secondName)
  }

  implicit val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    val minNum = 0
    val maxNum = 999999

    for {
      prefix <- Gen.oneOf(Nino.validPrefixes)
      number <- Gen.choose(minNum, maxNum)
      suffix <- Gen.oneOf(Nino.validSuffixes)
    } yield Nino(f"$prefix$number%06d$suffix")
  }

  implicit val arbitraryUtr: Arbitrary[UniqueTaxpayerReference] = Arbitrary {
    val minT        = 0
    val maxT        = 9
    val givenLength = 10

    for {
      value <- Gen.listOfN(givenLength, Gen.chooseNum(minT, maxT)).map(_.mkString)
    } yield UniqueTaxpayerReference(value)
  }

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    val startYear       = 1900
    val startMonth      = 1
    val startDayOfMonth = 1
    val endYear         = 2100
    val endMonth        = 1
    val endDayOfMonth   = 1

    datesBetween(LocalDate.of(startYear, startMonth, startDayOfMonth), LocalDate.of(endYear, endMonth, endDayOfMonth))
  }

  implicit val arbitraryRequestCommon: Arbitrary[RequestCommon] = Arbitrary {

    for {
      receiptDate        <- nonEmptyString
      acknowledgementRef <- stringsWithMaxLength(stringMaxLen)

    } yield RequestCommon(
      receiptDate = receiptDate,
      regime = "CRFA",
      acknowledgementReference = acknowledgementRef,
      None
    )
  }

  implicit val arbitraryRegistration: Arbitrary[RegisterWithoutId] = Arbitrary {
    for {
      requestCommon  <- arbitrary[RequestCommon]
      name           <- nonEmptyString
      address        <- arbitrary[Address]
      contactDetails <- arbitrary[ContactDetails]
      identification <- Gen.option(arbitrary[Identification])
      isAGroup       <- arbitrary[Boolean]
      isAnAgent      <- arbitrary[Boolean]
    } yield RegisterWithoutId(
      RegisterWithoutIDRequest(
        requestCommon,
        RequestDetails(
          Some(NoIdOrganisation(name)),
          None,
          address = address,
          contactDetails = contactDetails,
          identification = identification,
          isAGroup = isAGroup,
          isAnAgent = isAnAgent
        )
      )
    )
  }

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      addressLine1 <- nonEmptyString
      addressLine2 <- Gen.option(nonEmptyString)
      addressLine3 <- nonEmptyString
      addressLine4 <- Gen.option(nonEmptyString)
      postalCode   <- Gen.option(nonEmptyString)
      countryCode  <- nonEmptyString
    } yield Address(
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      postalCode = postalCode,
      countryCode = countryCode
    )
  }

  implicit val arbitraryContactDetails: Arbitrary[ContactDetails] = Arbitrary {
    for {
      phoneNumber  <- Gen.option(nonEmptyString)
      mobileNumber <- Gen.option(nonEmptyString)
      faxNumber    <- Gen.option(nonEmptyString)
      emailAddress <- Gen.option(nonEmptyString)
    } yield ContactDetails(
      phoneNumber = phoneNumber,
      mobileNumber = mobileNumber,
      faxNumber = faxNumber,
      emailAddress = emailAddress
    )
  }

  implicit val arbitraryIdentification: Arbitrary[Identification] = Arbitrary {
    for {
      idNumber           <- nonEmptyString
      issuingInstitution <- nonEmptyString
      issuingCountryCode <- nonEmptyString
    } yield Identification(
      idNumber = idNumber,
      issuingInstitution = issuingInstitution,
      issuingCountryCode = issuingCountryCode
    )
  }

  implicit val arbitraryPayloadRegisterWithID: Arbitrary[RegisterWithID] =
    Arbitrary {
      for {
        registerWithIDRequest <- arbitrary[RegisterWithIDRequest]
      } yield RegisterWithID(registerWithIDRequest)
    }

  implicit val arbitraryRegisterWithIDRequest: Arbitrary[RegisterWithIDRequest] = Arbitrary {
    for {
      requestCommon <- arbitrary[RequestCommon]
      requestDetail <- arbitrary[RequestWithIDDetails]
    } yield RegisterWithIDRequest(requestCommon, requestDetail)
  }

  implicit val arbitraryRequestWithIDDetails: Arbitrary[RequestWithIDDetails] =
    Arbitrary {
      for {
        idType            <- nonEmptyString
        idNumber          <- nonEmptyString
        requiresNameMatch <- arbitrary[Boolean]
        isAnAgent         <- arbitrary[Boolean]
        partnerDetails <- Gen.option(
          Gen.oneOf(
            arbitrary[WithIDIndividual],
            arbitrary[WithIDOrganisation]
          )
        )
      } yield RequestWithIDDetails(
        idType,
        idNumber,
        requiresNameMatch,
        isAnAgent,
        partnerDetails
      )
    }

  implicit val arbitraryWithIDIndividual: Arbitrary[WithIDIndividual] =
    Arbitrary {
      for {
        firstName   <- nonEmptyString
        middleName  <- Gen.option(nonEmptyString)
        lastName    <- nonEmptyString
        dateOfBirth <- Gen.option(nonEmptyString)
      } yield WithIDIndividual(firstName, middleName, lastName, dateOfBirth)
    }

  implicit val arbitraryWithIDOrganisation: Arbitrary[WithIDOrganisation] =
    Arbitrary {
      for {
        organisationName <- nonEmptyString
        organisationType <- Gen.oneOf(
          Seq("0000", "0001", "0002", "0003", "0004")
        )
      } yield WithIDOrganisation(organisationName, organisationType)
    }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] =
    Arbitrary {
      for {
        firstName  <- nonEmptyString
        middleName <- Gen.option(nonEmptyString)
        lastName   <- nonEmptyString
      } yield IndividualDetails(
        firstName = firstName,
        middleName = middleName,
        lastName = lastName
      )
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] =
    Arbitrary {
      for {
        name <- nonEmptyString
      } yield OrganisationDetails(name = name)
    }

  implicit val arbitraryContactInformationForIndividual: Arbitrary[ContactInformationForIndividual] = Arbitrary {
    for {
      individual <- arbitrary[IndividualDetails]
      email      <- nonEmptyString
      phone      <- Gen.option(nonEmptyString)
      mobile     <- Gen.option(nonEmptyString)
    } yield ContactInformationForIndividual(
      individual,
      email,
      phone,
      mobile
    )
  }

  implicit val arbitraryContactInformationForOrganisation: Arbitrary[ContactInformationForOrganisation] = Arbitrary {
    for {
      organisation <- arbitrary[OrganisationDetails]
      email        <- nonEmptyString
      phone        <- Gen.option(nonEmptyString)
      mobile       <- Gen.option(nonEmptyString)
    } yield ContactInformationForOrganisation(
      organisation,
      email,
      phone,
      mobile
    )
  }

  implicit val arbitraryCreateSubscriptionRequest: Arbitrary[CreateSubscriptionRequest] =
    Arbitrary {
      for {
        idType         <- nonEmptyString
        idNumber       <- nonEmptyString
        tradingName    <- Gen.option(nonEmptyString)
        gbUser         <- arbitrary[Boolean]
        primaryContact <- arbitrary[PrimaryContact]
      } yield CreateSubscriptionRequest(idType, idNumber, tradingName, gbUser, primaryContact, None)
    }

  implicit val arbitraryDisplaySubscriptionRequestDetail: Arbitrary[DisplaySubscriptionRequest] = Arbitrary {
    for {
      idNumber <- RegexpGen.from("[A-Z0-9]{1,15}")
    } yield DisplaySubscriptionRequest(
      idNumber = idNumber
    )
  }

  implicit val arbitraryPrimaryContact: Arbitrary[PrimaryContact] = Arbitrary {
    for {
      contactInformation <- Gen.oneOf(
        arbitrary[ContactInformationForIndividual],
        arbitrary[ContactInformationForIndividual]
      )
    } yield PrimaryContact(contactInformation)
  }

  implicit val arbitrarySecondaryContact: Arbitrary[SecondaryContact] =
    Arbitrary {
      for {
        contactInformation <- Gen.oneOf(
          arbitrary[ContactInformationForIndividual],
          arbitrary[ContactInformationForIndividual]
        )
      } yield SecondaryContact(contactInformation)
    }

  implicit val arbitraryUpdateSubscriptionRequest: Arbitrary[UpdateSubscriptionRequest] =
    Arbitrary {
      for {
        idType           <- nonEmptyString
        idNumber         <- nonEmptyString
        gbUser           <- arbitrary[Boolean]
        tradingName      <- Gen.option(nonEmptyString)
        primaryContact   <- arbitrary[PrimaryContact]
        secondaryContact <- Gen.option(arbitrary[SecondaryContact])
      } yield UpdateSubscriptionRequest(idType, idNumber, gbUser, primaryContact, tradingName, secondaryContact)
    }

}
