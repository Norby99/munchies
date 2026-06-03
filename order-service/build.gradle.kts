import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":order-shared"))
  implementation("io.micronaut.kafka:micronaut-kafka")

  testImplementation(testFixtures(project(":architecture-rules")))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.order.MainKt"
}
