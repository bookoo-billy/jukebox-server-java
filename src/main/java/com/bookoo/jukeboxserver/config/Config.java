package com.bookoo.jukeboxserver.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {

    @Value("${postgres.db.url}")
    private String postgresDbUrl;

    @Value("${postgres.db.password}")
    private String postgresDbPassword;

    @Value("${postgres.db.user}")
    private String postgresDbUser;

    @Bean
    public Connection dbConnection() throws SQLException {
        return DriverManager.getConnection(postgresDbUrl, postgresDbUser, postgresDbPassword);
    }
}