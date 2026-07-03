package com.munchies.restaurant.bdd

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

/**
 * Configuration for Cucumber tests in Micronaut
 *
 * This test runner executes all feature files found in src/test/resources/features
 * and maps them to step definitions in com.munchies.restaurant.bdd package
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
  key = "cucumber.object-factory",
  value = "com.munchies.restaurant.bdd.MicronautObjectFactory",
)
@MicronautTest
class CucumberRunnerTest
