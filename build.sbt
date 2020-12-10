import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }

import Dependencies._

lazy val commonSettings = Seq(
  scalaVersion := scala212,
  test in assembly := {},
  assemblyJarName in assemblyPackageDependency := "deps.jar",
  assemblyJarName in assembly := "app.jar",
  assemblyOption in assembly := (assemblyOption in assembly).value
    .copy(includeScala = false, includeDependency = false),
  assemblyMergeStrategy in assembly := {
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.discard
    case x if x.endsWith("logback.xml")                  => MergeStrategy.last
    case x if x.endsWith("module-info.class")            => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  organization := "io.findify",
  updateOptions := updateOptions.value.withCachedResolution(true),
  parallelExecution in ThisBuild := false,
  parallelExecution in Test := false,
  logBuffered in Test := false,
  testOptions in Test ++= Seq(
    Tests.Argument("-oT"),
    Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports")
  )
) ++ Testing.settings

lazy val common = (project in file("fopsy-common"))
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val apiRlProtobuf = (project in file("api-rl-protobuf"))
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val fp = (project in file("fp"))
  .enablePlugins(DockerPlugin)
  .dependsOn(common % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val lucy = (project in file("lucy"))
  .enablePlugins(GitVersioning)
  .enablePlugins(DockerPlugin)
  .dependsOn(fp % "test->test;it->it;compile->compile")
  .dependsOn(common % "test->test;it->it;compile->compile")
  .dependsOn(merlin % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val flink = (project in file("flink"))
  .dependsOn(common % "test->test;it->it;compile->compile")
  .dependsOn(merlin % "test->test;it->it;compile->compile")
  .dependsOn(apiRlProtobuf % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)
  .enablePlugins(DockerPlugin)

lazy val merlin = (project in file("merlin"))
  .dependsOn(common % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)
  .enablePlugins(DockerPlugin)

lazy val fpapi = (project in file("fpapi"))
  .enablePlugins(DockerPlugin)
  .dependsOn(common % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val pushapi = (project in file("pushapi"))
  .enablePlugins(DockerPlugin)
  .dependsOn(lucy % "test->test;it->it;compile->compile")
  .dependsOn(fp % "test->test;it->it;compile->compile")
  .dependsOn(common % "test->test;it->it;compile->compile")
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val benchmark = (project in file("benchmark"))
  .dependsOn(lucy % "test->test;it->it;compile->compile")
  .dependsOn(flink % "test->test;it->it;compile->compile")
  .enablePlugins(JmhPlugin)
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val loadTest = (project in file("load-test"))
  .enablePlugins(GatlingPlugin)
  .settings(commonSettings: _*)
  .configs(Configs.all: _*)

lazy val root = (project in file("."))
  .aggregate(fp, lucy, flink, fpapi, pushapi, common, merlin, apiRlProtobuf)
  .settings(
    name := "Backend"
  )
  .settings(Testing.settings: _*)
  .configs(Configs.all: _*)

parallelExecution in Global := false
updateOptions := updateOptions.value.withCachedResolution(true)

lazy val dumpVersion = taskKey[Unit]("write current version into a file")

// Note: because this isn't a function it will stay the same the entire session, if you use sbt shell
git.formattedShaVersion := git.gitHeadCommit.value map {
  case sha if git.gitUncommittedChanges.value => s"${sha.take(7)}-${System.currentTimeMillis.hashCode}"
  case sha                                    => sha.take(7)
}

dumpVersion := {
  Files.write(Paths.get(".version.txt"), version.value.getBytes(StandardCharsets.UTF_8))
}

scalaVersion := scala212
