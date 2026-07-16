plugins {
  id("com.vanniktech.maven.publish")
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  pom {
    url.set("https://github.com/Norby99/munchies")
    licenses {
      license {
        name.set("Apache-2.0")
        url.set("https://opensource.org/licenses/Apache-2.0")
      }
    }
    developers {
      developer {
        id.set("munchies")
        name.set("Munchies Team")
      }
    }
    scm {
      url.set("https://github.com/Norby99/munchies")
    }
  }
}
