name         := "my-spray-2"

version      := "1.0"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository"    at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaVersion      = "2.3.9"
  val sprayVersion     = "1.3.3"
  Seq(
    "io.spray"               %% "spray-can"                     % sprayVersion,
    "io.spray"               %% "spray-routing"                 % sprayVersion,
    "io.spray"               %% "spray-httpx"                   % sprayVersion,
    "io.spray"               %% "spray-testkit"                 % sprayVersion  % "test",
    "com.typesafe.akka"      %% "akka-actor"                    % akkaVersion,
    "com.typesafe.akka"      %% "akka-testkit"                  % akkaVersion   % "test",
    "com.typesafe.akka"      %% "akka-persistence-experimental" % akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"                    % akkaVersion,
    "com.typesafe.akka"      %% "akka-remote"                   % akkaVersion,
    "com.typesafe.akka"      %% "akka-multi-node-testkit"       % akkaVersion   % "test",
    "ch.qos.logback"         %  "logback-classic"               % "1.0.13",
    "org.specs2"             %% "specs2-core"                   % "2.3.11"      % "test",
    "net.hamnaberg.rest"     %% "scala-json-collection"         % "2.3",
    "org.json4s"             %% "json4s-native"                 % "3.2.11",
    //"org.apache.camel"     %  "camel-scala"                   % "2.14.1",
    "com.github.t3hnar"      %% "scala-bcrypt"                  % "2.4",
    "com.sksamuel.elastic4s" %% "elastic4s"                     % "1.4.13",
    "com.github.nscala-time" %% "nscala-time" % "1.8.0"
  )
}

//mainClass:= Some("com.example.BackendMain")

scalariformSettings

Revolver.settings
