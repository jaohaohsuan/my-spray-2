lazy val hello = taskKey[Unit]("Prints 'Hello World'")

lazy val root = (project in file(".")).
  settings(
    name         := "my-spray-2",
    version      := "1.0",
    scalaVersion := "2.11.5",
    hello        := { println("hello world!") }
  )

scalacOptions += "-deprecation"

libraryDependencies ++= {
  val akkaVersion      = "2.3.9"
  val sprayVersion     = "1.3.2"
  Seq(
    "io.spray"           %% "spray-can"                     % sprayVersion,
    "io.spray"           %% "spray-routing"                 % sprayVersion,
    "io.spray"           %% "spray-httpx"                 % sprayVersion,
    "io.spray"           %% "spray-testkit"                 % sprayVersion  % "test",
    "com.typesafe.akka"  %% "akka-actor"                    % akkaVersion,
    "com.typesafe.akka"  %% "akka-testkit"                  % akkaVersion   % "test",
    "com.typesafe.akka"  %% "akka-persistence-experimental" % akkaVersion,
    "org.specs2"         %% "specs2-core"                   % "2.3.11"      % "test",
    "net.hamnaberg.rest" %% "scala-json-collection"         % "2.3",
    "org.json4s"         %% "json4s-native"                 % "3.2.11"
  )
}

Revolver.settings
