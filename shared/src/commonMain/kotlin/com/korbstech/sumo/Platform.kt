package com.korbstech.sumo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform