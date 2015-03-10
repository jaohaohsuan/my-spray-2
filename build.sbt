import com.typesafe.sbt.SbtScalariform.scalariformSettings

name         := "my-spray-2"

version      := "1.0"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion      = "2.3.9"
  val sprayVersion     = "1.3.2"
  Seq(
    "io.spray"           %% "spray-can"                     % sprayVersion,
    "io.spray"           %% "spray-routing"                 % sprayVersion,
    "io.spray"           %% "spray-httpx"                   % sprayVersion,
    "io.spray"           %% "spray-testkit"                 % sprayVersion  % "test",
    "com.typesafe.akka"  %% "akka-actor"                    % akkaVersion,
    "com.typesafe.akka"  %% "akka-testkit"                  % akkaVersion   % "test",
    "com.typesafe.akka"  %% "akka-persistence-experimental" % akkaVersion,
    "org.specs2"         %% "specs2-core"                   % "2.3.11"      % "test",
    "net.hamnaberg.rest" %% "scala-json-collection"         % "2.3",
    "org.json4s"         %% "json4s-native"                 % "3.2.11",
    //"org.apache.camel" %  "camel-scala"                   % "2.14.1",
    "org.slf4j"          %  "slf4j-simple"                  % "1.6.4",
    "com.github.t3hnar"  %% "scala-bcrypt"                  % "2.4"
  )
}

scalariformSettings

Revolver.settings
