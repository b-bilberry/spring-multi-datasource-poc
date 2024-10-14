package io.bilberry.poc.database.dynamic.datasource.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.zaxxer.hikari.HikariDataSource
import io.bilberry.poc.database.dynamic.datasource.config.DatabaseConfiguration
import io.bilberry.poc.database.dynamic.datasource.model.DatabaseSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object DbConfigurationHelper {

    private val logger: Logger = LoggerFactory.getLogger(DbConfigurationHelper::class.java)

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    fun createHikariDataSources(databaseConfiguration: DatabaseConfiguration): Map<Any, Any> {
        logger.info("Trying to create a list of HikariDataSource")
        val hikariDataSources = mutableMapOf<Any, Any>()
        when (databaseConfiguration.externalConfiguration.isEmpty()) {
            true -> {
                logger.info("External database configuration is empty")
                hikariDataSources["ALL"] = HikariDataSource().apply {
                    jdbcUrl = databaseConfiguration.primary.jdbcUrl
                    username = databaseConfiguration.primary.username
                    password = databaseConfiguration.primary.password
                    driverClassName = "org.postgresql.Driver"
                }
            }

            false -> {
                logger.info("Found an external database configuration")
                val externalConfiguration = this::class.java
                    .getResource(databaseConfiguration.externalConfiguration)
                    ?.readText().orEmpty()

                check(externalConfiguration.isNotEmpty()) {
                    "External database configuration is empty"
                }

                objectMapper.readValue<List<DatabaseSettings>>(externalConfiguration).forEach {
                    hikariDataSources[it.flowId] = HikariDataSource().apply {
                        jdbcUrl = it.jdbcUrl
                        username = it.username
                        password = it.password
                        driverClassName = "org.postgresql.Driver"
                    }
                }
            }
        }
        logger.info("Done with ${hikariDataSources.size} HikariDataSource instance")
        return hikariDataSources
    }
}