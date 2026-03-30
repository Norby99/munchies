import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(libs.lang4j.core)
  implementation(libs.lang4j.ollama)
  implementation(libs.lang4j.gemini)

  implementation(project(":commons"))
  implementation(project(":suggestion-service:shared"))
  testImplementation(testFixtures(project(":architecture-rules")))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.suggestion.MainKt"
}
