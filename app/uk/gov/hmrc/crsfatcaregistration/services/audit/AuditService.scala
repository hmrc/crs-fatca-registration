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

package uk.gov.hmrc.crsfatcaregistration.services.audit

import play.api.Logger
import play.api.libs.json.OWrites
import uk.gov.hmrc.crsfatcaregistration.models.audit.{AuditEvent, CreateRegistration, UpdateContactDetails}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (
  auditConnector: DefaultAuditConnector
)(implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass)

  def sendCreateRegistration(
    auditRequest: CreateRegistration
  )(implicit hc: HeaderCarrier): Unit =
    send(
      auditType = "CreateRegistration",
      event = auditRequest
    )

  def sendUpdateContactDetails(
    auditRequest: UpdateContactDetails
  )(implicit hc: HeaderCarrier): Unit =
    send(
      auditType = "UpdateContactDetails",
      event = auditRequest
    )

  private def send[E <: AuditEvent](
    auditType: String,
    event: E
  )(implicit
    hc: HeaderCarrier,
    writes: OWrites[E]
  ): Unit = {
    logger.info(s"Auditing $auditType")

    auditConnector.sendExplicitAudit(
      auditType = auditType,
      detail = event
    )
  }

}
