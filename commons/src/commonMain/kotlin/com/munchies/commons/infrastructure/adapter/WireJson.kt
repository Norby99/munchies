package com.munchies.commons.infrastructure.adapter

import kotlinx.serialization.json.Json

// Every toJson() across the shared modules must encode through this instance, never the bare
// kotlinx.serialization Json.Default. Json.Default has encodeDefaults = false, which silently
// omits any field currently sitting at its Kotlin default value (false, 0, "", empty collection)
// from the encoded JSON — including when a wrapper (e.g. a gateway route) decodes an incoming
// request and immediately re-encodes it to forward downstream: the field can be dropped even
// though the original caller sent it explicitly. Every receiving service expects the field to be
// present on the wire, so defaults must always be encoded.
val wireJson: Json = Json { encodeDefaults = true }
