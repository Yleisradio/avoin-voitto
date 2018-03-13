lazy val akkaHttpVersion = "10.0.7"
lazy val akkaVersion = "2.5.1"
lazy val json4sV = "3.4.2"
lazy val scalednVersion = "1.0.0-e8180d08620a607ec47613f8c2585f7784e86625"

lazy val root = (project in file(".")).
  settings(
    resolvers ++= List(
      Resolver.bintrayRepo("unisay", "maven"),
      Resolver.bintrayRepo("mandubian", "maven")
    ),
    inThisBuild(List(
      organization := "fi.yle",
      scalaVersion := "2.11.11"
    )),
    name := "liiga-voitto",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "org.json4s" %% "json4s-native" % json4sV,
      "org.json4s" %% "json4s-jackson" % json4sV,
      "org.json4s" %% "json4s-ext" % json4sV,
      "com.mandubian" %% "scaledn-parser" % scalednVersion,

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.github.unisay" %% "mockserver-client-scala" % "0.2.0" % Test,
      "org.mock-server" % "mockserver-netty" % "3.10.4" % Test
    )
  )
