package com.munchies.restaurant.bdd

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
class CucumberRunnerTest
