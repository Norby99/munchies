import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":order-service:api"))
  testImplementation(testFixtures(project(":architecture-rules")))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.user.MainKt"
}
