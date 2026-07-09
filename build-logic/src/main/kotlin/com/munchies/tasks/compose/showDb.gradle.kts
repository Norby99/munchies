package com.munchies.tasks.compose

import com.bmuschko.gradle.docker.tasks.container.DockerExecContainer

plugins {
  id("com.bmuschko.docker-remote-api")
}

tasks.register<DockerExecContainer>("composeShowDb") {
  group = "compose"
  description = "Shows MongoDB data for a service. " +
    "Usage: ./gradlew composeShowDb -Pservice=<name> [-Pcollection=<name>]"

  val serviceName = project.findProperty("service") as? String
    ?: throw GradleException("You must specify a service: ./gradlew composeShowDb -Pservice=<name>")
  val collection = project.findProperty("collection") as? String

  targetContainerId("munchies-mongo-$serviceName")

  val mongoScript = if (collection != null) {
    "db.getSiblingDB('$serviceName').$collection.find().forEach(printjson)"
  } else {
    "print('Collections:'); " +
      "db.getSiblingDB('$serviceName').getCollectionNames().forEach(c => print(' - ' + c))"
  }

  commands.add(arrayOf("mongosh", "--quiet", "--eval", mongoScript))
}
