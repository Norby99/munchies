package com.munchies.user.domain.port

import java.util.*

typealias TimeProvider = () -> Long
fun defaultTimeProvider(): TimeProvider = { System.currentTimeMillis() }

private const val ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000L
fun TimeProvider.addOneHour(): TimeProvider = { this() + ONE_HOUR_IN_MILLISECONDS }
fun TimeProvider.toDate(): Date = Date(this())
