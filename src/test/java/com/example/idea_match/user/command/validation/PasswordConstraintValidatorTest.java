package com.example.idea_match.user.command.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordConstraintValidatorTest {

    private PasswordConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordConstraintValidator();
        validator.initialize(null);
    }

    @Test
    void shouldReturnTrueForValidPassword() {
        // given
        String validPassword = "Password123!";

        // when
        boolean result = validator.isValid(validPassword, context);

        // then
        assertThat(result).isTrue();
        verifyNoInteractions(context);
    }

    @Test
    void shouldReturnFalseForPasswordTooShort() {
        // given
        String shortPassword = "Pass1!";
        setupMockContext();

        // when
        boolean result = validator.isValid(shortPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordTooLong() {
        // given
        String longPassword = "A".repeat(31) + "1!";
        setupMockContext();

        // when
        boolean result = validator.isValid(longPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordWithoutUppercase() {
        // given
        String noUppercasePassword = "password123!";
        setupMockContext();

        // when
        boolean result = validator.isValid(noUppercasePassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordWithoutLowercase() {
        // given
        String noLowercasePassword = "PASSWORD123!";
        setupMockContext();

        // when
        boolean result = validator.isValid(noLowercasePassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordWithoutDigit() {
        // given
        String noDigitPassword = "Password!";
        setupMockContext();

        // when
        boolean result = validator.isValid(noDigitPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordWithoutSpecialCharacter() {
        // given
        String noSpecialPassword = "Password123";
        setupMockContext();

        // when
        boolean result = validator.isValid(noSpecialPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForPasswordWithWhitespace() {
        // given
        String whitespacePassword = "Password 123!";
        setupMockContext();

        // when
        boolean result = validator.isValid(whitespacePassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForNullPassword() {
        // given
        String nullPassword = null;
        setupMockContext();

        // when
        boolean result = validator.isValid(nullPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldReturnFalseForEmptyPassword() {
        // given
        String emptyPassword = "";
        setupMockContext();

        // when
        boolean result = validator.isValid(emptyPassword, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void shouldAcceptVariousSpecialCharacters() {
        // given & when & then
        String[] validPasswords = {
                "Password123!",
                "Password123@",
                "Password123#",
                "Password123$",
                "Password123%",
                "Password123^",
                "Password123&",
                "Password123*"
        };

        for (String password : validPasswords) {
            boolean result = validator.isValid(password, context);
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldAcceptMinimumLengthPassword() {
        // given
        String minLengthPassword = "Pass123!";

        // when
        boolean result = validator.isValid(minLengthPassword, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAcceptMaximumLengthPassword() {
        // given
        String maxLengthPassword = "P".repeat(13) + "p".repeat(13) + "123!";

        // when
        boolean result = validator.isValid(maxLengthPassword, context);

        // then
        assertThat(result).isTrue();
    }

    private void setupMockContext() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }
}