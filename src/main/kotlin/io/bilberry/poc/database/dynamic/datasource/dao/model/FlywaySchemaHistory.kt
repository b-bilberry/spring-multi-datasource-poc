package io.bilberry.poc.database.dynamic.datasource.dao.model

internal data class FlywaySchemaHistory(
    val version: String?,
    val script: String,
    val success: Boolean
)