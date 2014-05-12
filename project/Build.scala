import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "slumbot"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "mysql" % "mysql-connector-java" % "5.1.21",
      jdbc,
      anorm,
      "com.socrata" % "soda-consumer-scala_2.9.2" % "1.0.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      testOptions in Test += Tests.Argument("junitxml", "console")
    )

}
