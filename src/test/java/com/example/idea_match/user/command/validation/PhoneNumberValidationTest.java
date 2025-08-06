package com.example.idea_match.user.command.validation;

import com.example.idea_match.user.command.RegisterUserCommand;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+48123456789",
            "+1234567890",
            "+123456789012",
            "+99999999999",
            "123456789",
            "1234567890123"
    })
    void shouldAcceptValidPhoneNumbers(String phoneNumber) {
        // given
        RegisterUserCommand command = createValidCommand(phoneNumber);

        // when
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

        // then
        Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(phoneViolations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0123456789",        // starts with 0
            "+0123456789",       // starts with +0
            "1",                 // too short
            "12345678901234567", // too long
            "+",                 // just plus
            "abc123456789",      // contains letters
            "+48-123-456-789",   // contains hyphens
            "+48 123 456 789",   // contains spaces
            "++48123456789",     // double plus
            "48123456789a"       // ends with letter
    })
    void shouldRejectInvalidPhoneNumbers(String phoneNumber) {
        // given
        RegisterUserCommand command = createValidCommand(phoneNumber);

        // when
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

        // then
        Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(phoneViolations).isNotEmpty();
        assertThat(phoneViolations.iterator().next().getMessage())
                .isEqualTo("Invalid phone number format");
    }

    @Test
    void shouldAcceptPhoneNumberWithMinimumLength() {
        // given - minimum valid international number (2 digits after country code)
        RegisterUserCommand command = createValidCommand("+122");

        // when
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

        // then
        Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(phoneViolations).isEmpty();
    }

    @Test
    void shouldAcceptPhoneNumberWithMaximumLength() {
        // given - maximum valid international number (14 digits after first)
        RegisterUserCommand command = createValidCommand("+12345678901234");

        // when
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

        // then
        Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(phoneViolations).isEmpty();
    }

    @Test
    void shouldAcceptNullPhoneNumber() {
        // given
        RegisterUserCommand command = new RegisterUserCommand(
                "johndoe",
                "john@example.com",
                "Password123!",
                "John",
                "Doe",
                null // null phone number should be allowed
        );

        // when
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

        // then
        Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(phoneViolations).isEmpty(); // null should be accepted
    }

    @Test
    void shouldAcceptVariousCountryCodes() {
        // given
        String[] validNumbers = {
                "+1234567890",    // US/Canada format
                "+48123456789",   // Poland
                "+33123456789",   // France
                "+49123456789",   // Germany
                "+44123456789",   // UK
                "+81123456789",   // Japan
                "+86123456789",   // China
                "+91123456789"    // India
        };

        for (String phoneNumber : validNumbers) {
            // when
            RegisterUserCommand command = createValidCommand(phoneNumber);
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(command);

            // then
            Set<ConstraintViolation<RegisterUserCommand>> phoneViolations = violations.stream()
                    .filter(v -> v.getPropertyPath().toString().equals("phoneNumber"))
                    .collect(java.util.stream.Collectors.toSet());

            assertThat(phoneViolations)
                    .as("Phone number %s should be valid", phoneNumber)
                    .isEmpty();
        }
    }

    private RegisterUserCommand createValidCommand(String phoneNumber) {
        return new RegisterUserCommand(
                "johndoe",
                "john@example.com",
                "Password123!",
                "John",
                "Doe",
                phoneNumber
        );
    }
}