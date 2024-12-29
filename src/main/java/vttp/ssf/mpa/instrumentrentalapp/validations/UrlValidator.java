package vttp.ssf.mpa.instrumentrentalapp.validations;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {
    
    private static final String URL_PATTERN = "^(https?:\\/\\/)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(\\/[^\\s]*)?\\.(jpg|jpeg|png)$";

    @Override
    public boolean isValid(String profilePic, ConstraintValidatorContext context) {

        // validate each URL in the list, allowing empty strings to pass
        Pattern pattern = Pattern.compile(URL_PATTERN); // compile the regex pattern

        // allow null/empty
        return profilePic == null || profilePic.trim().isEmpty() || pattern.matcher(profilePic).matches();
    }

}
