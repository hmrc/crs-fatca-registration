import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.hmrc" %% "domain-play-30" % "9.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion,
    "org.mockito"             %% "mockito-scala"              % "1.17.31",
    "wolfendale"              %% "scalacheck-gen-regexp"      % "0.1.2",
    "org.jsoup"                % "jsoup"                      % "1.17.2",
    "org.scalatest"           %% "scalatest"                  % "3.2.18",
    "com.github.tomakehurst"   % "wiremock-standalone"        % "3.0.1",
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0"
  ).map(_ % "test, it")

}
