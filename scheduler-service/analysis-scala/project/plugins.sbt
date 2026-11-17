addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")

// Lets `Test / jsEnv` run specs under Node instead of the default (nonexistent-here) browser env.
libraryDependencies += "org.scala-js" %% "scalajs-env-nodejs" % "1.6.0"
