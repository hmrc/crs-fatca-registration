import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val microservice = Project("crs-fatca-registration", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(update / evictionWarningOptions :=
    EvictionWarningOptions.default.withWarnScalaVersionEviction(false))
  .settings(
    majorVersion        := 0,
    scalaVersion        := "2.13.10",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    ThisBuild / scalafmtOnCompile.withRank(KeyRanks.Invisible) := true,
    scalacOptions ++= Seq(
          "-Wconf:src=routes/.*:s",
          "-Wconf:src=.+/test/.+:s",
          "-Wconf:cat=deprecation&msg=\\.*()\\.*:s",
          "-Wconf:cat=unused-imports&site=<empty>:s",
          "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
          "-Wconf:cat=unused&src=.*Routes\\.scala:s"
      )
  ).settings(
      ThisBuild / libraryDependencySchemes ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
      )
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)

