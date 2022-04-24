val scala3Version = "3.1.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "EVMLog4s",
    organization := "com.hoangong",
    version := sys.env
      .getOrElse(
        "VERSION",
        "0.0.1-" + sys.env.getOrElse("GITHUB_SHA", "noci") + "-SNAPSHOT"
      )
      .replace("v", "")
      .replace("V", ""),
    scalaVersion := scala3Version,
    credentials += Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      "1mdc",
      sys.env.getOrElse("PUBLISH_GITHUB_TOKEN", "")
    ),
    publishTo := Some(
      "GitHub Package Registry" at "https://maven.pkg.github.com/1mdc/EVMLog4s/"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % Versions.munitVersion % Test,
      "org.web3j" % "core" % Versions.web3jVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLoggingVersion,
      "com.squareup.okhttp3" % "okhttp" % Versions.okhttpVersion
    )
  )
