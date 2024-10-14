package io.bilberry.poc.database.dynamic.datasource.service

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

internal class RoutingDatasource : AbstractRoutingDataSource(){
    override fun determineCurrentLookupKey(): String = DatasourceContextHolder.getDatasourceIdentifier()
}