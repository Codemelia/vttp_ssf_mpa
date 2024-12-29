package vttp.ssf.mpa.instrumentrentalapp.validations;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UrlsValidator implements ConstraintValidator<ValidUrls, List<String>> {

    private static final String URL_PATTERN = "^(https?:\\/\\/)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(\\/[^\\s]*)?\\.(jpg|jpeg|png)$";

    @Override
    public boolean isValid(List<String> instrumentPics, ConstraintValidatorContext context) {

        // validate each URL in the list, allowing empty strings to pass
        Pattern pattern = Pattern.compile(URL_PATTERN); // compile the regex pattern

        return instrumentPics.stream()
            .allMatch(url -> url == null || url.trim().isEmpty() || pattern.matcher(url).matches());
    }
    
}
