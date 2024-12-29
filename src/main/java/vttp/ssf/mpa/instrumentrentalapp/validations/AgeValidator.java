package vttp.ssf.mpa.instrumentrentalapp.validations;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {
    
    private int minAge;
    private int maxAge;

    @Override
    public void initialize(ValidAge ageValue) {
        this.minAge = ageValue.minAge();
        this.maxAge = ageValue.maxAge();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true; // allow null values
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= minAge && age <= maxAge;
    }
    
}
