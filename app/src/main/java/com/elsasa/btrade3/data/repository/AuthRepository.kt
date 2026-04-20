package com.elsasa.btrade3.data.repository

import com.elsasa.btrade3.domain.model.ValidationResult

interface AuthRepository {
    suspend fun isSalesPersonValid(email: String): ValidationResult
}

