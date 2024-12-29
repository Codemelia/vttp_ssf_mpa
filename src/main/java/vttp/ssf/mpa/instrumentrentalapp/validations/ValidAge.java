package vttp.ssf.mpa.instrumentrentalapp.validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
// import jakarta.validation.Payload;
import jakarta.validation.Payload;

@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER}) // target form field and param passed into controller
@Retention(RetentionPolicy.RUNTIME) // ensure availability at runtime
public @interface ValidAge {
    // set default message if not set
    String message() default "Age must be between {minAge} and {maxAge} years";
    
    // define validation groups
    // {} means no specific group associated
    Class<?>[] groups() default {};

    // attach additional metadata to constraint
    // {} means no additional payload provided
    Class<? extends Payload>[] payload() default {};

    // set default min and max age if not set
    int minAge() default 16;
    int maxAge() default 100;
}
