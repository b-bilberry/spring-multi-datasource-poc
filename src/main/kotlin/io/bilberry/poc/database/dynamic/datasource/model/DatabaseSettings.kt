package io.bilberry.poc.database.dynamic.datasource.model

internal data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String,
    val password: String? = null,
    val flowId: String
)