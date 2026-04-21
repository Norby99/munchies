import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  implementation("io.micronaut.kafka:micronaut-kafka")
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.order.MainKt"
}
