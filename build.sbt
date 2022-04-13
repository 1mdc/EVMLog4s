val scala3Version = "3.1.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ethereum wrapper for scala",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.web3j" % "core" % "4.9.0",
    libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.9.3",
    libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
)
