package com.willowtreeapps.common.util

import com.willowtreeapps.common.Logger


fun <T> profile(label: String, work: () -> T): T {
    val start = TimeUtil.systemTimeMs()
    val value = work()
    Logger.d("$label: ${TimeUtil.systemTimeMs() - start}ms")
    return value
}