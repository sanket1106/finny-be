package com.finny.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmountValidatorTest {

    private AmountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AmountValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "100.5", "100.55", "-100", "-100.55"})
    void testIsValid_Success(String amount) {
        assertTrue(validator.isValid(new BigDecimal(amount), null));
    }

    @Test
    void testIsValid_Null_ReturnsTrue() {
        // @NotNull handles nulls, validator should not fail on null
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void testIsValid_Zero_ReturnsFalse() {
        assertFalse(validator.isValid(BigDecimal.ZERO, null));
        assertFalse(validator.isValid(new BigDecimal("0.00"), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.555", "0.001", "-0.001", "10.1234"})
    void testIsValid_TooManyDecimals_ReturnsFalse(String amount) {
        assertFalse(validator.isValid(new BigDecimal(amount), null));
    }
}
