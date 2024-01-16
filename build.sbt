val zioVersion       = "2.0.20"
val ironVersion      = "2.0.0"
val scalaCsv         = "1.3.10"
val scala3Version    = "3.3.1"
val scalatestVersion = "3.2.17"

ThisBuild / organization := "fr.efrei"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version

lazy val root = project
  .in(file("."))
  .settings(
    name := "electricity-analysis",
    libraryDependencies ++= Seq(
      "dev.zio"              %% "zio"         % zioVersion,
      "dev.zio"              %% "zio-streams" % zioVersion,
      "io.github.iltotore"   %% "iron"        % ironVersion,
      "com.github.tototoshi" %% "scala-csv"   % scalaCsv
    ),
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio-test"     % zioVersion       % Test,
      "dev.zio"       %% "zio-test-sbt" % zioVersion       % Test,
      "org.scalatest" %% "scalatest"    % scalatestVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
