package de.kimrudolph.routing;

import de.kimrudolph.routing.entities.Customer;
import de.kimrudolph.routing.repositories.CustomerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RoutingTestConfiguration.class)
public class RoutingApplicationTests {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    RoutingTestUtil routingTestUtil;

    @Autowired
    CacheManager cacheManager;

    @Test
    public void contextSwitchTest() throws Exception {

        // Create databases for each environment
        for (DatabaseEnvironment databaseEnvironment : DatabaseEnvironment
            .values()) {
            routingTestUtil.createDatabase(databaseEnvironment);
        }

        // Create a customer in each environment
        for (DatabaseEnvironment databaseEnvironment : DatabaseEnvironment
            .values()) {
            DatabaseContextHolder.set(databaseEnvironment);
            Customer devCustomer = new Customer();
            devCustomer.setName("Tony Tester");
            customerRepository.save(devCustomer);
            DatabaseContextHolder.clear();
        }

        // Every customer entry is the first entry
        for (DatabaseEnvironment databaseEnvironment : DatabaseEnvironment
            .values()) {
            DatabaseContextHolder.set(databaseEnvironment);
            assertEquals(1L,
                customerRepository.findOneByName("Tony Tester").getId()
                    .longValue());
            DatabaseContextHolder.clear();
        }

        // Check if caches are filled for each environment
        for (DatabaseEnvironment databaseEnvironment : DatabaseEnvironment
            .values()) {
            DatabaseContextHolder.set(databaseEnvironment);
            assertEquals("Tony Tester",
                ((Customer) cacheManager.getCache("customers")
                    .get(databaseEnvironment + "-findOneByName-Tony Tester")
                    .get()).getName());
            DatabaseContextHolder.clear();
        }

    }

}
