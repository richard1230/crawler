package Chapter3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateISBNTest {

    /**
     *
     * 这里的两个测试方法主要是测试
     * checkISBN这个方法
     * **/

    @Test
    public void checkAValidISBN(){
        ValidateISBN validateISBN = new ValidateISBN();
        boolean result = validateISBN.checkISBN("0140449116");
        assertTrue(result,"first value");
        result = validateISBN.checkISBN("0140177396");
        assertTrue(result,"second value");
    }

    @Test
    public void checkAnInvalidISBN() {
        ValidateISBN validator = new ValidateISBN();
        //0140449116+1
        boolean result = validator.checkISBN("0140449117");
        assertFalse(result);
    }

}
