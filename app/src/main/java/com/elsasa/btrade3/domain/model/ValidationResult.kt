package com.elsasa.btrade3.domain.model

sealed class ValidationResult {
    data class Success(val isValid: Boolean) : ValidationResult()
    data class Failure(val reason: String) : ValidationResult()
}

