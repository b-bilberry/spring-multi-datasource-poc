package io.bilberry.poc.database.dynamic.datasource.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "database")
@Validated
internal open class DatabaseConfiguration {
    var primary = PrimaryDatabaseConfiguration()
    var externalConfiguration: String = ""
}