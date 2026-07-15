package com.munchies.user.domain.port

import java.util.*

/**
 * Functional abstraction that supplies the current time in milliseconds.
 */
typealias TimeProvider = () -> Long

/**
 * Returns a time provider backed by the system clock.
 */
fun defaultTimeProvider(): TimeProvider = { System.currentTimeMillis() }

private const val ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000L

/**
 * Returns a new time provider advanced by one hour from the current provider value.
 */
fun TimeProvider.addOneHour(): TimeProvider = { this() + ONE_HOUR_IN_MILLISECONDS }

/**
 * Converts the provider value to a Java [Date].
 */
fun TimeProvider.toDate(): Date = Date(this())
