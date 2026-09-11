plugins {
  id("micronaut-library")
}

dependencies {
  implementation("io.micronaut:micronaut-management")
  implementation("io.micronaut.mongodb:micronaut-mongo-sync")
  implementation("org.mongodb:mongodb-driver-sync")
}
