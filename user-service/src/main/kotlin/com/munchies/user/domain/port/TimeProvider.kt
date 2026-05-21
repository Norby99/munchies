package com.munchies.user.domain.port

typealias TimeProvider = () -> Long
fun defaultTimeProvider(): TimeProvider = { System.currentTimeMillis() }

private const val ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000L
fun TimeProvider.addOneHour(): Long = this() + ONE_HOUR_IN_MILLISECONDS
