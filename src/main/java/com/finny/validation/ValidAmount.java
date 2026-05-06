package com.finny.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {
    String message() default "Amount must be non-zero and have at most 2 decimal places";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
