package com.finny.service;

import com.finny.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
public class TenantProvisioningService {

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

    public void provisionTenantDatabase(String dbName, String tenantId) {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(urlPrefix + dbName + urlSuffix)
                .username(username)
                .password(password)
                .build();

        LocalContainerEntityManagerFactoryBean emFactoryBean = new LocalContainerEntityManagerFactoryBean();
        emFactoryBean.setDataSource(dataSource);
        emFactoryBean.setPackagesToScan("com.finny.domain", "com.finny.domain.enums");
        emFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        // We don't want multitenancy for this specific setup connection
        emFactoryBean.setJpaPropertyMap(properties);

        emFactoryBean.afterPropertiesSet();

        EntityManagerFactory emf = emFactoryBean.getObject();
        if (emf != null) {
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                seedCategories(em, tenantId);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new RuntimeException("Failed to seed categories", e);
            } finally {
                em.close();
            }
        }
        
        emFactoryBean.destroy();
    }

    private void seedCategories(EntityManager em, String tenantId) {
        // Income Categories
        createCategory(em, tenantId, "Salary", null);
        createCategory(em, tenantId, "Bonus", null);
        createCategory(em, tenantId, "Interest", null);
        createCategory(em, tenantId, "Dividends", null);
        createCategory(em, tenantId, "Rental Income", null);

        // Expense Categories
        createCategory(em, tenantId, "Housing", null);
        createCategory(em, tenantId, "Transportation", null);
        createCategory(em, tenantId, "Food", null);
        createCategory(em, tenantId, "Utilities", null);
        createCategory(em, tenantId, "Insurance", null);
        createCategory(em, tenantId, "Healthcare", null);
        createCategory(em, tenantId, "Savings", null);
        createCategory(em, tenantId, "Personal Spending", null);
        createCategory(em, tenantId, "Entertainment", null);
        createCategory(em, tenantId, "Education", null);
    }

    private void createCategory(EntityManager em, String tenantId, String name, Category parent) {
        Category category = new Category();
        category.setTenantId(tenantId);
        category.setName(name);
        category.setParent(parent);
        em.persist(category);
    }
}
