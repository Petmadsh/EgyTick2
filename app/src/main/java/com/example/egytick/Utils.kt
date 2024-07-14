package com.example.egytick

// Helper function to extract resource name
fun extractResourceName(imagePath: String): String {
    return imagePath.substringAfterLast("/").substringBeforeLast(".")
}
