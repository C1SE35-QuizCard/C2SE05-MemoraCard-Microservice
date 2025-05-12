package com.example.quizcards.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 100), // Password length between 8 and 30 characters
                new CharacterRule(EnglishCharacterData.UpperCase, 1), // At least one uppercase letter
                new CharacterRule(EnglishCharacterData.LowerCase, 1), // At least one lowercase letter
                new CharacterRule(EnglishCharacterData.Digit, 1), // At least one digit
                new CharacterRule(EnglishCharacterData.Special, 1), // At least one special character
                new WhitespaceRule() // No whitespace allowed
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
//        context.buildConstraintViolationWithTemplate(
//                        String.join(" ", validator.getMessages(result)))
//                .addConstraintViolation();
        context.buildConstraintViolationWithTemplate(validator.getMessages(result).get(0))
                .addConstraintViolation();
        return false;
    }
}
