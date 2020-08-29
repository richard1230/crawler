package Java_in_action.chapter2.Tags;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@Tag("repository")
public class CustomerRepositoryTest {
    private String CUSTOMER_NAME = "John Smith";
    private CustomerRepository reposity = new CustomerRepository();

    @Test
    void testNonExistence(){
        boolean exists = reposity.contains("John Smith");
        assertFalse(exists);
    }

    @Test
    void testCustomerPersistence(){
        reposity.persist(new Customer(CUSTOMER_NAME));
        assertTrue(reposity.contains("John Smith"));
    }


}