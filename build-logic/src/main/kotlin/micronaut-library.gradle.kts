import utils.MUNCHIES_BASE_PACKAGE

plugins {
  id("kotlin-jvm")
  id("org.jetbrains.kotlin.plugin.allopen")
  id("com.google.devtools.ksp")
  id("io.micronaut.library")
}

micronaut {
  processing {
    incremental(true)
    annotations("$MUNCHIES_BASE_PACKAGE.*")
  }
}

allOpen {
  annotation("io.micronaut.aop.Around")
}
