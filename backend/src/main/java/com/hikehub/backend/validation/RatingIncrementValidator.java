package com.hikehub.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RatingIncrementValidator implements ConstraintValidator<RatingIncrement, Double> {
    
    @Override
    public void initialize(RatingIncrement constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        // Check if rating * 2 is an integer (which means it's a 0.5 increment)
        double doubled = value * 2.0;
        return Math.abs(doubled - Math.round(doubled)) < 0.0001; // Account for floating point precision
    }
}

