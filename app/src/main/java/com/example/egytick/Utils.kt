package com.example.egytick

fun extractResourceName(imagePath: String): String {
    return imagePath.substringAfterLast('/').substringBeforeLast('.').lowercase()
}
