package Java_in_action.chapter2.lifecycle;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SUTTest {
    private static ResourceForAllTests resourceForAllTests;
    private SUT systemUnderTest;

    @BeforeAll//所有的@Test的方法开始之前运行的,所有方法里面它是第一个运行的
    static void setUpClass() {
        resourceForAllTests = new ResourceForAllTests("Our resource for all tests");
    }

    @AfterAll//所有的@Test的方法运行结束之后运行的，所有方法里面它是最后一个运行的
    static void tearDownClass() {
        resourceForAllTests.close();
    }

    @BeforeEach//每个有@Test的方法开始之前都需要运行,这个例子里面需要运行两次,有两个@Test方法
    void setUp() {
        systemUnderTest = new SUT("Our system under test");
    }

    @AfterEach//每个有@Test的方法结束之后都需要运行,这个例子里面需要运行两次,有两个@Test方法
    void tearDown() {
        systemUnderTest.close();
    }

    @Test
    void testRegularWork() {
        boolean canReceiveRegularWork = systemUnderTest.canReceiveRegularWork();

        assertTrue(canReceiveRegularWork);
    }

    @Test
    void testAdditionalWork() {
        boolean canReceiveAdditionalWork = systemUnderTest.canReceiveAdditionalWork();

        assertFalse(canReceiveAdditionalWork);
    }

}