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

// Plain-JVM sibling of `root`: everything in `kitchen.model`/`kitchen.agent` and the
// vendored u06-u08 code is platform-agnostic Scala already -- `kitchen/JsApi.scala` is
// the *only* file that touches scala.scalajs.js (confirmed by grep, not assumed), so
// this project reuses root's sources/specs verbatim, minus that one file, and adds its
// own JVM-only directory (`jvm/src/main/scala`) for things that genuinely can't exist
// on the ScalaJS/Node target: real OS-thread parallelism via java.util.concurrent,
// which Scala.js's JDK emulation doesn't provide (Node is single-threaded).
//
// Purpose: `sbt test` on `root` runs the compiled JS inside a real Node process, which
// needs sbt's own JVM and a spawned Node process resident at once -- the README already
// flags this as liable to an OOM-guard kill under memory pressure, with `Test/compile`
// (type-checking only) as the fallback. `analysisJVM/test` is a third option: the exact
// same specs, actually executed, on the JVM alone -- faster and lighter than the Node
// path, and the place `ParallelCapacityAgent`'s many-seed Monte Carlo scans get a real
// multi-core speedup that the production ScalaJS build structurally cannot offer.
lazy val analysisJVM = (project in file("jvm"))
  .settings(
    name := "scheduler-analysis-jvm",
    Compile / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / ".." / "src" / "main" / "scala",
      baseDirectory.value / "src" / "main" / "scala",
    ),
    Compile / excludeFilter := "JsApi.scala",
    Test / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "test" / "scala",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    ),
    fork := true,
    javaOptions ++= Seq("-Xmx3g"),
  )
