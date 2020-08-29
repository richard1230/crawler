package Chapter4;

import Chapter3.ValidateISBN;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateISBNTest {

    @Test
    public void checkAValidISBN(){
        Chapter3.ValidateISBN validateISBN = new Chapter3.ValidateISBN();
        boolean result = validateISBN.checkISBN("0140449116");
        assertTrue(result,"first value");
        result = validateISBN.checkISBN("0140177396");
        assertTrue(result,"second value");
    }

    @Test
    public void checkAnInvalidISBN() {
        Chapter3.ValidateISBN validator = new ValidateISBN();
        //0140449116+1
        boolean result = validator.checkISBN("0140449117");
        assertFalse(result);
    }

    @Test
    public void nineDigitISBNsAreNotAllowed(){
        ValidateISBN validator = new ValidateISBN();


    }

}