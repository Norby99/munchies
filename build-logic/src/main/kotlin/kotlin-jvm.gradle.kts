plugins {
  kotlin("jvm")
}

val javaVersion: String by project
kotlin {
  jvmToolchain(javaVersion.toInt())
}
