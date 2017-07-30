package cz.mycom.veeam.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.mysql.jdbc.Driver;
import cz.mycom.veeam.portal.service.KeyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

/**
 * @author dursik
 */
@Slf4j
@SpringBootApplication
@ComponentScan("cz.mycom.veeam.portal")
@EnableJpaRepositories(basePackages = "cz.mycom.veeam.portal.repository")
@EnableWebMvc
@EnableCaching
@EnableRetry
public class AppConfig {

    @Autowired
    private Environment env;

    @Autowired
    private DataSource dataSource;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @PostConstruct
    public void initializeDatabasePropertySourceUsage() {
        MutablePropertySources propertySources = ((ConfigurableEnvironment) env).getPropertySources();
        try {
            DatabaseConfiguration config = new DatabaseConfiguration(dataSource,"portal_config", "name", "value");
            Properties dbProps = ConfigurationConverter.getProperties(config);
            PropertiesPropertySource dbPropertySource = new PropertiesPropertySource("dbPropertySource", dbProps);
            propertySources.addFirst(dbPropertySource);
        } catch (Exception e) {
            log.error("Error during database properties setup", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://portal.cjgvultbz6oy.eu-west-1.rds.amazonaws.com:3306/portal");
        dataSource.setPassword("wDZCtKhNMz68Fhrk");
        dataSource.setUsername("portal");
        return dataSource;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                .failOnUnknownProperties(false)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(jpaVendorAdapter());
        factory.setPackagesToScan("cz.mycom.veeam.portal.model");
        factory.setDataSource(dataSource);
        factory.setJpaProperties(initHibernateProperties());
        return factory;
    }

    @Bean
    public Properties initHibernateProperties() {
        Properties properties = new Properties();
        // properties.put("hibernate.dialect", environment.getRequiredProperty("hb.dialect"));
//        properties.put("hibernate.show_sql", environment.getRequiredProperty("hb.show_sql"));
//        properties.put("hibernate.format_sql", environment.getRequiredProperty("hb.format_sql"));
//        properties.put("hibernate.generate_statistics", environment.getRequiredProperty("hb.generate_statistics"));
        return properties;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.MYSQL);
        return vendorAdapter;
    }

    @Bean
    public KeyStoreService keyStoreService() {
        return new KeyStoreService();
    }
}
