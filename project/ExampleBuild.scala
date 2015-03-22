import sbt._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm }

object ExampleBuild extends Build {

  lazy val buildSettings = Defaults.defaultSettings ++ multiJvmSettings ++ Seq(
  crossPaths := false
  )

  lazy val example = Project(
  id = "my-spray-2",
  base = file("."),
  settings = buildSettings ++ Project.defaultSettings
  ) configs(MultiJvm)

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),

    parallelExecution in Test := false,

    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiJvmResults) =>
        val overall =
          if(testResults.overall.id < multiJvmResults.overall.id) multiJvmResults.overall
          else testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiJvmResults.events,
          testResults.summaries ++ multiJvmResults.summaries)
    }
  )
}