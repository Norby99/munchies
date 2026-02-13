plugins {
  kotlin("multiplatform")
}

kotlin {
  js {
    browser {
      testTask {
        enabled = false
      }
    }
    nodejs()
    binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
      }
    }
    val commonTest by getting {
      dependencies {
      }
    }
    val jsMain by getting {
    }
  }
}
