package com.example.naepapp

import com.example.naepapp.core.common.InputValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun isValidEmail_validEmailFormats_returnsTrue() {
        assertTrue(InputValidator.isValidEmail("user@example.com"))
        assertTrue(InputValidator.isValidEmail("john.doe@domain.co.uk"))
        assertTrue(InputValidator.isValidEmail("test+label@domain.org"))
    }

    @Test
    fun isValidEmail_invalidEmailFormats_returnsFalse() {
        assertFalse(InputValidator.isValidEmail(""))
        assertFalse(InputValidator.isValidEmail("user@"))
        assertFalse(InputValidator.isValidEmail("user@domain"))
        assertFalse(InputValidator.isValidEmail("user.domain.com"))
        assertFalse(InputValidator.isValidEmail("@domain.com"))
    }

    @Test
    fun isValidPhone_validPhoneNumbers_returnsTrue() {
        assertTrue(InputValidator.isValidPhone("+265991234567"))
        assertTrue(InputValidator.isValidPhone("0991234567"))
        assertTrue(InputValidator.isValidPhone("+1 (555) 123-4567"))
    }

    @Test
    fun isValidPhone_invalidPhoneNumbers_returnsFalse() {
        assertFalse(InputValidator.isValidPhone(""))
        assertFalse(InputValidator.isValidPhone("12345"))
        assertFalse(InputValidator.isValidPhone("abc123456789"))
    }

    @Test
    fun isValidFullName_validAndInvalidNames() {
        assertTrue(InputValidator.isValidFullName("John Doe"))
        assertTrue(InputValidator.isValidFullName("Al"))
        assertFalse(InputValidator.isValidFullName("A"))
        assertFalse(InputValidator.isValidFullName("  "))
    }

    @Test
    fun isValidPassword_validAndInvalidPasswords() {
        assertTrue(InputValidator.isValidPassword("12345678"))
        assertTrue(InputValidator.isValidPassword("securepassword"))
        assertFalse(InputValidator.isValidPassword("short"))
        assertFalse(InputValidator.isValidPassword(""))
    }
}
