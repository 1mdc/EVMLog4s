val scala3Version = "3.1.1"


lazy val root = project
  .in(file("."))
  .settings(
    name := "EVMLog4s",
    organization := "com.hoangong",
    version := "1.0.0",
    scalaVersion := scala3Version,
    githubOwner := "1mdc",
    githubRepository := "EVMLog4s",
    githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % Versions.munitVersion % Test,
      "org.web3j" % "core" % Versions.web3jVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLoggingVersion,
      "com.squareup.okhttp3" % "okhttp" % Versions.okhttpVersion
    )
  )
