package io.github.composeguard.sample.model

data class Product(
    val id: String,
    val name: String,
    val priceCents: Int,
    val isAvailable: Boolean
)

data class User(
    val id: String,
    val displayName: String
)
