package com.finny.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull handles null
        }
        // Must be non-zero
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        // Must have at most 2 decimal places
        return value.stripTrailingZeros().scale() <= 2;
    }
}
