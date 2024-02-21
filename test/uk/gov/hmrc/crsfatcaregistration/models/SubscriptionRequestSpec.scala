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

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.crsfatcaregistration.generators.Generators

class SubscriptionRequestSpec extends AnyFreeSpec with Generators with OptionValues with Matchers {

  "SubscriptionRequest" - {
    "must serialise and de-serialise as expected" in {

      val requestModel: CreateSubscriptionRequest =
        arbitrary[CreateSubscriptionRequest].sample.value

      Json
        .toJson(requestModel)
        .as[CreateSubscriptionRequest] mustBe requestModel

    }
  }

  "SubscriptionResponse" - {
    "must serialise and match example response" in {
      Json
        .obj(
          "success" -> Json.obj(
            "processingDate" -> "2001-12-17T09:30:47Z",
            "subscriptionID" -> "XACRS0000123456"
          )
        )
        .as[SubscriptionResponse] mustBe SubscriptionResponse("XACRS0000123456", "2001-12-17T09:30:47Z")
    }
  }

}
