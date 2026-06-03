import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.order.MainKt"
}
