package Java_in_action.chapter2.displayname;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class displaynameTest {
    private SUT  systemUnderTest = new SUT();

    @Test
    @DisplayName("Our system under test says hello.")
    void testHello(){
        assertEquals("Hello",systemUnderTest.hello());
    }

    @Test
    @DisplayName("haha")
    void testTalking(){
        assertEquals("How are you?",systemUnderTest.talk());
    }

    @Test
    void testBye(){
        assertEquals("Bye",systemUnderTest.bye());
    }


}