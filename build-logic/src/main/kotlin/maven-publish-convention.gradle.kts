plugins {
  id("com.vanniktech.maven.publish")
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  coordinates(
    groupId = "io.github.norby99",
    version = (findProperty("versionName") as String?) ?: project.version.toString(),
  )

  pom {
    name.set(project.name)
    description.set("${project.name} module of the Munchies microservices platform")
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
