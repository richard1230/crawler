package Java_in_action.chapter2.nested;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class NestedTestsTest {

        private static final String First_NAME = "John";
        private static final String LAST_NAME = "Smith";

        @Nested
        public class BuilderTest{
            private String MIDDLE_NAME = "Michael";

            @Test
            void customerBuilder() throws ParseException {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                Date customerDate = simpleDateFormat.parse("04-21-2019");

                Customer customer = new Customer.Builder(Gender.MALE,First_NAME,LAST_NAME)
                        .withMiddleName(MIDDLE_NAME)
                        .withBecomeCustomer(customerDate)
                        .build();

                assertAll(()->{
                    assertEquals(Gender.MALE,customer.getGender());
                    assertEquals(First_NAME,customer.getFirstName());
                    assertEquals(LAST_NAME,customer.getLastName());
                    assertEquals(MIDDLE_NAME,customer.getMiddleName());
                    assertEquals(customerDate,customer.getBecomeCustomer());
                });
            }
        }

}