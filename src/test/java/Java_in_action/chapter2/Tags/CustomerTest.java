package Java_in_action.chapter2.Tags;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("individual")
class CustomerTest {
    private String CUSTOMER_NAME = "John Smith";

    @Test
    void testCustomer(){
        Customer customer = new Customer(CUSTOMER_NAME);
        assertEquals("John Smith",customer.getName());
    }

}