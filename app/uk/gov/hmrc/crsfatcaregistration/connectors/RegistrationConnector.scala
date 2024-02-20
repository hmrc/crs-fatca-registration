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

package uk.gov.hmrc.crsfatcaregistration.connectors

import com.google.inject.Inject
import uk.gov.hmrc.crsfatcaregistration.config.AppConfig
import uk.gov.hmrc.crsfatcaregistration.models.{RegisterWithID, RegisterWithoutId}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject() (
  val config: AppConfig,
  val http: HttpClient
) {

  def sendWithoutIDInformation(
    registration: RegisterWithoutId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "register-without-id"
    http.POST[RegisterWithoutId, HttpResponse](
      config.baseUrl(serviceName),
      registration,
      headers = extraHeaders(config, serviceName)
    )(wts = RegisterWithoutId.format, rds = httpReads, hc = hc, ec = ec)
  }

  def sendWithID(
    registration: RegisterWithID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "register-with-id"

    http.POST[RegisterWithID, HttpResponse](
      config.baseUrl(serviceName),
      registration,
      headers = extraHeaders(config, serviceName)
    )(wts = RegisterWithID.format, rds = httpReads, hc = hc, ec = ec)
  }

}
