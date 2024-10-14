
package io.bilberry.poc.database.dynamic.datasource

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.bilberry.poc.database.dynamic.datasource.config.DatabaseConfiguration
import io.bilberry.poc.database.dynamic.datasource.dao.mapper.FlywayMapper
import io.bilberry.poc.database.dynamic.datasource.service.DatasourceContextHolder
import io.bilberry.poc.database.dynamic.datasource.service.DbConfigurationHelper
import io.bilberry.poc.database.dynamic.datasource.service.RoutingDatasource
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean

@SpringBootApplication
@ConfigurationPropertiesScan
internal open class DynamicDatasourceApplication {

    @Bean
    open fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    @Bean
    open fun routingDataSource(
        databaseConfiguration: DatabaseConfiguration
    ): RoutingDatasource {
        val dataSources = DbConfigurationHelper.createHikariDataSources(databaseConfiguration)
        val primaryDataSource = dataSources["ALL"]
        checkNotNull(primaryDataSource)
        val routingDataSource = RoutingDatasource()
        routingDataSource.setTargetDataSources(dataSources)
        routingDataSource.setDefaultTargetDataSource(primaryDataSource)
        return routingDataSource
    }

    @Bean
    open fun myBatisConfig(): Configuration {
        return Configuration().apply {
            addMapper(FlywayMapper::class.java)
        }
    }

    @Bean
    open fun sqlSessionFactory(
        routingDatasource: RoutingDatasource,
        config: Configuration
    ): SqlSessionFactory {
        val sqlSessionFactoryBean = SqlSessionFactoryBean()
        sqlSessionFactoryBean.setDataSource(routingDatasource)
        sqlSessionFactoryBean.setConfiguration(config)
        return sqlSessionFactoryBean.`object`!!
    }

    @Bean
    open fun flywayMapper(sqlSessionFactory: SqlSessionFactory): FlywayMapper {
        val sessionTemplate = SqlSessionTemplate(sqlSessionFactory)
        return sessionTemplate.getMapper(FlywayMapper::class.java)
    }

    @Bean
    open fun runner(flywayMapper: FlywayMapper): CommandLineRunner {
        return CommandLineRunner { args ->
            val flowId = args.singleOrNull() ?: "ALL"
            DatasourceContextHolder.setDatasourceIdentifier(flowId)
            logger.info("Will use the following flowId: $flowId")
            logger.info("Starting processing...")
            flywayMapper.getSchemaHistory().forEachIndexed { index, flyway ->
                logger.info("Record #$index: Version: ${flyway.version}, Script: ${flyway.script}, Success: ${flyway.success}")
            }
            logger.info("DONE")
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(DynamicDatasourceApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(DynamicDatasourceApplication::class.java)
                .bannerMode(Banner.Mode.CONSOLE)
                .web(WebApplicationType.NONE)
                .run(*args)
                .close()
        }
    }
}