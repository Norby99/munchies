import utils.libs

plugins {
  id("kotlin-jvm")
  id("java-test-fixtures")
}

dependencies {
  implementation(libs().konsist)
  implementation(libs().kotest)
}
