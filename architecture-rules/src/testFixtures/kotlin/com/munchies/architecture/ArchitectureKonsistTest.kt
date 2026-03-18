package com.munchies.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import io.kotest.core.spec.style.AnnotationSpec

open class ArchitectureKonsistTest(val service: String) : AnnotationSpec() {
  @Test
  fun `should follow architecture rules`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        val basePackage = "com.munchies"
        val domainLayer = Layer("Domain", "$basePackage.$service.domain..")
        domainLayer.dependsOnNothing()
      }
  }
}
