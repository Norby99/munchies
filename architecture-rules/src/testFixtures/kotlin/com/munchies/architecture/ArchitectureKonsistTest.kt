package com.munchies.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.AnnotationSpec

open class ArchitectureKonsistTest(private val service: String) : AnnotationSpec() {
  private val basePackage = "com.munchies"

  val commonsLayer = Layer("commons", "$basePackage.commons..")
  val domainLayer = Layer("domain", "$basePackage.$service.domain..")
  val applicationLayer = Layer("application", "$basePackage.$service.application..")
  val infrastructureLayer = Layer("infrastructure", "$basePackage.$service.infrastructure..")

  @Test
  fun `domain layer should depend on nothing except commons`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        commonsLayer.dependsOnNothing()
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
  fun `infrastructure layer should depend on domain layer`() {
    Konsist
      .scopeFromProject()
      .assertArchitecture {
        infrastructureLayer.dependsOn(applicationLayer, domainLayer)
      }
  }

  @Test
  fun `classes with 'UseCase' suffix should reside in 'application-usecase' package `() {
    Konsist
      .scopeFromProject()
      .classes()
      .withNameEndingWith("UseCase")
      .assertTrue { it.resideInPackage("..application.usecase..") }
  }
}
