import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":user-service:api"))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.user.MainKt"
}
