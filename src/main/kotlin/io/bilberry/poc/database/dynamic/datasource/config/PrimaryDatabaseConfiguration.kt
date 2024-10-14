package io.bilberry.poc.database.dynamic.datasource.config

internal open class PrimaryDatabaseConfiguration {
    lateinit var jdbcUrl: String
    lateinit var username: String
    lateinit var password: String
}