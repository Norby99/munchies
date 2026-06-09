import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  testImplementation(testFixtures(project(":architecture-rules")))

  implementation(project(":user-shared"))

  implementation(project(":payment-shared")) {
    targetConfiguration = "jvmRuntimeElements"
  }

  implementation(libs.java.jwt)
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.user.MainKt"
}
