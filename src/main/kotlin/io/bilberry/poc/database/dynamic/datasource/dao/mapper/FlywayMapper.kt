package io.bilberry.poc.database.dynamic.datasource.dao.mapper

import io.bilberry.poc.database.dynamic.datasource.dao.model.FlywaySchemaHistory

internal interface FlywayMapper {
    fun getSchemaHistory(): List<FlywaySchemaHistory>
}