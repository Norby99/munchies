import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("micronaut-server")
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":order-shared"))
  // Menu contracts (paths, DTOs, JSON parsers) used to read item prices from restaurant-service.
  implementation(project(":restaurant-shared"))

  testImplementation(testFixtures(project(":architecture-rules")))
  // The tracing component test attaches its own appender to check which trace a line belongs to.
  "componentTestImplementation"("ch.qos.logback:logback-classic")
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.order.MainKt"
}
