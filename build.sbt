import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val microservice = Project("crs-fatca-registration", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(update / evictionWarningOptions :=
    EvictionWarningOptions.default.withWarnScalaVersionEviction(false))
  .settings(
    majorVersion        := 0,
    scalaVersion        := "2.13.10",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 10031,
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
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-common"
)

lazy val itSettings = integrationTestSettings() ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-common"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork := true
)

