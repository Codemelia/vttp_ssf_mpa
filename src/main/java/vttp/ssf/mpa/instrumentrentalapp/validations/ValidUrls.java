package vttp.ssf.mpa.instrumentrentalapp.validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = UrlsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER}) // target form field and param passed into controller
@Retention(RetentionPolicy.RUNTIME) // set available at runtime
public @interface ValidUrls {
    
    // set default message if not set
    String message() default "Image URLs must be in valid JPG or PNG format";

    // define validation groups
    // {} means no specific group associated
    Class<?>[] groups() default {};

    // attach additional metadata to constraint
    // {} means no additional payload provided
    Class<? extends Payload>[] payload() default {};

}
