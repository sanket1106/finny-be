package com.finny.config;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import com.finny.repository.master.UserRepository;

@Component
public class UserConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

    private final DataSource defaultDataSource;
    private final Map<String, DataSource> tenantDataSources = new HashMap<>();

    @Value("${finny.tenant.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Value("${finny.tenant.datasource.url-prefix:jdbc:mysql://localhost:3306/}")
    private String urlPrefix;

    @Value("${finny.tenant.datasource.url-suffix:?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false}")
    private String urlSuffix;

    @Value("${finny.tenant.datasource.username:root}")
    private String username;

    @Value("${finny.tenant.datasource.password:password}")
    private String password;

    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public UserConnectionProvider(DataSource defaultDataSource, ObjectProvider<UserRepository> userRepositoryProvider) {
        this.defaultDataSource = defaultDataSource;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return defaultDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return tenantDataSources.computeIfAbsent(tenantIdentifier, this::createDataSource);
    }

    private DataSource createDataSource(String userId) {
        String dbName;
        if ("default_common".equals(userId)) {
            dbName = "finny_master";
        } else {
            UserRepository userRepository = userRepositoryProvider.getIfAvailable();
            if (userRepository == null) {
                throw new IllegalStateException("UserRepository not available yet to resolve tenant");
            }
            dbName = userRepository.findById(userId)
                    .map(user -> user.getTenant().getDbName())
                    .orElseThrow(() -> new RuntimeException("Could not resolve tenant for user: " + userId));
        }

        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(urlPrefix + dbName + urlSuffix)
                .username(username)
                .password(password)
                .build();
    }
}
