package ru.romanow.orders.config

import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration
class DatabaseTestConfiguration {
    companion object {
        private const val POSTGRES_IMAGE = "postgres:13-alpine"
        private const val DATABASE_NAME = "orders"
        private const val USERNAME = "program"
        private const val PASSWORD = "test"
    }

    @Bean(destroyMethod = "close")
    fun postgres(): PostgreSQLContainer<*> {
        val postgres = PostgreSQLContainer<PostgreSQLContainer<*>>(POSTGRES_IMAGE)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withDatabaseName(DATABASE_NAME)
        postgres.start()
        return postgres
    }

    @Primary
    @DependsOn("postgres")
    @Bean(destroyMethod = "close")
    fun dataSource(): HikariDataSource {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = postgres().jdbcUrl
        dataSource.username = USERNAME
        dataSource.password = PASSWORD
        dataSource.driverClassName = Driver::class.java.canonicalName
        return dataSource
    }
}