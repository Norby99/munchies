import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("kotlin-server-stub")
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":user-service:api"))
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.user.MainKt"
}
