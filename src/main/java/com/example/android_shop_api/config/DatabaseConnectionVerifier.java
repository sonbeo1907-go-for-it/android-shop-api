package com.example.android_shop_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionVerifier implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseConnectionVerifier.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionVerifier(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        if (!Integer.valueOf(1).equals(result)) {
            throw new IllegalStateException(
                    "Không thể xác minh kết nối PostgreSQL"
            );
        }

        String databaseName = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
        );

        log.info(
                "PostgreSQL connection verified successfully. Database: {}",
                databaseName
        );
    }
}