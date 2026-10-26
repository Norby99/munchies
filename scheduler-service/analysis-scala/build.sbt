ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.4.0" // matches ../../asmd-public-models

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scheduler-analysis",
    // We export a library (kitchen.JsApi), not a program with a main() entry point.
    scalaJSUseMainModuleInitializer := false,
    // scheduler-service's package.json is "type": "commonjs" — match it.
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.20" % Test,
    ),
    Test / jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
  )
