import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = Project("crs-fatca-registration", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 10031,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    ThisBuild / scalafmtOnCompile.withRank(KeyRanks.Invisible) := true,
    scalacOptions ++= Seq(
      "-Wconf:cat=deprecation:w",
      "-Wconf:cat=feature:w",
      "-Wconf:src=target/.*:s"
    ),
    scalacOptions ++= Seq(
      "-Wconf:msg=unused.*&src=.*\\.routes:s",
      "-Wconf:src=.+/test/.+:s"
    ),
    scalacOptions ~= (_.distinct),
  )
  .settings(inConfig(Test)(testSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-common")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
addCommandAlias(
  "precommit",
  "; clean ; scalafmtAll ; coverage ; test ; it/test ; coverageReport ; coverageOff"
)