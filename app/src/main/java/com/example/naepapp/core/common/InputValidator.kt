package com.example.naepapp.core.common

object InputValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^[0-9+\\-\\s()]{9,}$")

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    fun isValidPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() }
        return PHONE_REGEX.matches(phone.trim()) && digitsOnly.length >= 9
    }

    fun isValidEmailOrPhone(identifier: String): Boolean {
        return isValidEmail(identifier) || isValidPhone(identifier)
    }

    fun isValidFullName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidPrice(price: String): Boolean {
        if (price.isBlank()) return false
        val doubleVal = price.toDoubleOrNull() ?: return false
        return doubleVal > 0
    }

    fun isValidInventory(count: String): Boolean {
        if (count.isBlank()) return false
        val intVal = count.toIntOrNull() ?: return false
        return intVal >= 0
    }
}
