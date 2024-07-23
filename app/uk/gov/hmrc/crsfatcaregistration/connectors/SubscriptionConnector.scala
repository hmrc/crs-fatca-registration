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

package uk.gov.hmrc.crsfatcaregistration.connectors

import com.google.inject.Inject
import uk.gov.hmrc.crsfatcaregistration.config.AppConfig
import uk.gov.hmrc.crsfatcaregistration.models.{CreateSubscriptionRequest, DisplaySubscriptionRequest, UpdateSubscriptionRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (
  val config: AppConfig,
  val http: HttpClient
) {

  def sendSubscriptionInformation(
    subscription: CreateSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "create-subscription"
    http.POST[CreateSubscriptionRequest, HttpResponse](
      config.baseUrl(serviceName),
      subscription,
      headers = extraHeaders(config, serviceName)
    )(
      wts = CreateSubscriptionRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

  def readSubscriptionInformation(
    params: DisplaySubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "read-subscription"
    http.GET[HttpResponse](
      s"${config.baseUrl(serviceName)}/${params.idNumber}",
      headers = extraHeaders(config, serviceName)
    )(
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

  def updateSubscriptionInformation(
    updateSubscriptionRequest: UpdateSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "update-subscription"
    http.PUT[UpdateSubscriptionRequest, HttpResponse](
      config.baseUrl(serviceName),
      updateSubscriptionRequest,
      headers = extraHeaders(config, serviceName)
    )(
      wts = UpdateSubscriptionRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

}
