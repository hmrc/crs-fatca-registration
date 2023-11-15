import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.20.0"
  private val hmrcMongoVersion = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "domain"                    % "8.3.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion,
    "org.mockito"             %% "mockito-scala"              % "1.16.42",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2",
    "wolfendale"              %% "scalacheck-gen-regexp"      % "0.1.2",
    "org.jsoup"                % "jsoup"                      % "1.12.1",
    "org.scalatest"           %% "scalatest"                  % "3.1.4",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "com.github.tomakehurst"   % "wiremock-standalone"        % "2.27.0",
    "com.vladsch.flexmark"     % "flexmark-all"               % "0.36.8"
  ).map(_ % "test, it")

}
