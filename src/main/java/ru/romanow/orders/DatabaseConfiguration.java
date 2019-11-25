package ru.romanow.orders;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories
public class DatabaseConfiguration {

    @Bean
    @Autowired
    @DependsOn("entityManagerFactory")
    public Flyway flyway(DataSource dataSource) {
        return Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:db/migrations")
                .outOfOrder(true)
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @Autowired
    public FlywayMigrationInitializer flywayInitializer(DataSource dataSource) {
        return new FlywayMigrationInitializer(flyway(dataSource));
    }
}
