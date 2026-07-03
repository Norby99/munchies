package com.munchies.restaurant.bdd

import io.cucumber.core.backend.ObjectFactory
import io.micronaut.context.ApplicationContext

class MicronautObjectFactory : ObjectFactory {
  private var context: ApplicationContext? = null

  override fun start() {
    context = ApplicationContext.run("test")
  }

  override fun stop() {
    context?.close()
  }

  override fun addClass(clazz: Class<*>?): Boolean = true

  override fun <T> getInstance(clazz: Class<T>): T {
    return context!!.getBean(clazz)
  }
}
