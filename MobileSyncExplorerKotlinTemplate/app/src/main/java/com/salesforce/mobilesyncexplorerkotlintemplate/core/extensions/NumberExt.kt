package com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions

fun UInt.coerceToPositiveInt(): Int = this.coerceAtMost(Int.MAX_VALUE.toUInt()).toInt()
