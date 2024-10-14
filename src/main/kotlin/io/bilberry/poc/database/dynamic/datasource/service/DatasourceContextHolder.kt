package io.bilberry.poc.database.dynamic.datasource.service

internal object DatasourceContextHolder {
    private val contextHolder = ThreadLocal<String>().apply {
        set("ALL")
    }

    fun setDatasourceIdentifier(identifier: String) {
        contextHolder.set(identifier)
    }

    fun getDatasourceIdentifier(): String = contextHolder.get()

    fun resetDatasourceIdentifier() = contextHolder.set("ALL")
}