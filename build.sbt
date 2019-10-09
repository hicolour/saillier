lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "saillier",
    organization := "com.prochera",
    version := "0.1",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"            % "2.0.0",
      "org.typelevel"        %% "cats-effect"          % "2.0.0",
      "org.typelevel"        %% "cats-mtl-core"        % "0.7.0",
      "io.monix"             %% "monix-execution"      % "3.0.0",
      "io.monix"             %% "monix"                % "3.0.0",
      "com.github.pureconfig" %% "pureconfig"          % "0.12.0",
      "org.scalactic"        %% "scalactic"            % "3.0.8"  % Test,
      "org.scalatest"        %% "scalatest"            % "3.0.+" % Test
    ),
    scalaVersion := "2.13.0",
    scalacOptions ++= List(
      "-unchecked",
      "-deprecation",
      "-Ymacro-annotations",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
