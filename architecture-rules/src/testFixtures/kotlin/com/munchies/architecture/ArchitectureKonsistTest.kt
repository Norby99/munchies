package com.munchies.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import io.kotest.core.spec.style.AnnotationSpec

open class ArchitectureKonsistTest(private val service: String) : AnnotationSpec() {
  private val basePackage = "com.munchies"

  val commonsLayer = Layer("commons", "$basePackage.commons..")
  val domainLayer = Layer("domain", "$basePackage.$service.domain..")
  val applicationLayer = Layer("application", "$basePackage.$service.application..")
  val presentationLayer = Layer("presentation", "$basePackage.$service.presentation..")

  @Test
  fun `domain layer should depend on nothing except commons`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        domainLayer.dependsOn(commonsLayer)
      }
  }

  @Test
  fun `application layer should depend on domain layer`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        applicationLayer.dependsOn(domainLayer)
      }
  }

  @Test
  fun `presentation layer should depend on domain layer`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        presentationLayer.dependsOn(domainLayer)
      }
  }
}
