package com.hikehub.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RatingIncrementValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RatingIncrement {
    String message() default "Rating must be in 0.5 increments";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

