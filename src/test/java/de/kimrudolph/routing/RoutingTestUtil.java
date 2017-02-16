package de.kimrudolph.routing;

import de.kimrudolph.routing.entities.Customer;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class RoutingTestUtil {

    public void createDatabase(
        DatabaseEnvironment environment) throws Exception {

        MetadataSources metadata = new MetadataSources(
            new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect",
                    "org.hibernate.dialect.H2Dialect")
                .applySetting(Environment.HBM2DDL_AUTO, "create").build());

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        for (BeanDefinition def : scanner
            .findCandidateComponents(Customer.class.getPackage().getName())) {
            metadata.addAnnotatedClass(Class.forName(def.getBeanClassName()));
        }

        Connection connection = DriverManager.getConnection(
            "jdbc:h2:mem:customer" + environment.name().toLowerCase()
                + ";DB_CLOSE_DELAY=-1", "sa", "");

        SchemaExport export =
            new SchemaExport((MetadataImplementor) metadata.buildMetadata(),
                connection);
        export.create(true, true);
    }
}
