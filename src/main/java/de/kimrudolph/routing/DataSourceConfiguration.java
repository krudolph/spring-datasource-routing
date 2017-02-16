package de.kimrudolph.routing;

import de.kimrudolph.routing.entities.Customer;
import de.kimrudolph.routing.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(basePackageClasses = CustomerRepository.class, entityManagerFactoryRef = "customerEntityManager", transactionManagerRef = "customerTransactionManager")
@EnableTransactionManagement
public class DataSourceConfiguration {

    @Autowired(required = false)
    private PersistenceUnitManager persistenceUnitManager;

    @Bean
    @Primary
    @ConfigurationProperties("app.customer.jpa")
    public JpaProperties customerJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.customer.development.datasource")
    public DataSource customerDevelopmentDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.customer.testing.datasource")
    public DataSource customerTestingDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.customer.production.datasource")
    public DataSource customerProductionDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Adds all available datasources to datasource map.
     *
     * @return datasource of current context
     */
    @Bean
    @Primary
    public DataSource customerDataSource() {
        DataSourceRouter router = new DataSourceRouter();

        final HashMap<Object, Object> map = new HashMap<>(3);
        map.put(DatabaseEnvironment.DEVELOPMENT,
            customerDevelopmentDataSource());
        map.put(DatabaseEnvironment.TESTING, customerTestingDataSource());
        map.put(DatabaseEnvironment.PRODUCTION, customerProductionDataSource());
        router.setTargetDataSources(map);
        return router;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean customerEntityManager(
        final JpaProperties customerJpaProperties) {

        EntityManagerFactoryBuilder builder =
            createEntityManagerFactoryBuilder(customerJpaProperties);

        return builder.dataSource(customerDataSource()).packages(Customer.class)
            .persistenceUnit("customerEntityManager").build();
    }

    @Bean
    @Primary
    public JpaTransactionManager customerTransactionManager(
        @Qualifier("customerEntityManager") final EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(
        JpaProperties customerJpaProperties) {
        JpaVendorAdapter jpaVendorAdapter =
            createJpaVendorAdapter(customerJpaProperties);
        return new EntityManagerFactoryBuilder(jpaVendorAdapter,
            customerJpaProperties.getProperties(), this.persistenceUnitManager);
    }

    private JpaVendorAdapter createJpaVendorAdapter(
        JpaProperties jpaProperties) {
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setDatabase(jpaProperties.getDatabase());
        adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return adapter;
    }
}