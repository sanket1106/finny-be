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
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        // We don't want multitenancy for this specific setup connection
        emFactoryBean.setJpaPropertyMap(properties);

        emFactoryBean.afterPropertiesSet();

        EntityManagerFactory emf = emFactoryBean.getObject();
        if (emf != null) {
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                seedCategories(em);
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

    private void seedCategories(EntityManager em) {
        // Income Categories
        createCategory(em, "Salary", null);
        createCategory(em, "Bonus", null);
        createCategory(em, "Interest", null);
        createCategory(em, "Dividends", null);
        createCategory(em, "Rental Income", null);

        // Expense Categories
        createCategory(em, "Housing", null);
        createCategory(em, "Transportation", null);
        createCategory(em, "Food", null);
        createCategory(em, "Utilities", null);
        createCategory(em, "Insurance", null);
        createCategory(em, "Healthcare", null);
        createCategory(em, "Savings", null);
        createCategory(em, "Personal Spending", null);
        createCategory(em, "Entertainment", null);
        createCategory(em, "Education", null);
    }

    private void createCategory(EntityManager em, String name, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setParent(parent);
        em.persist(category);
    }
}
