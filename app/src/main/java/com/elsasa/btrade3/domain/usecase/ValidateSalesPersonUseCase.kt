package com.elsasa.btrade3.domain.usecase

import com.elsasa.btrade3.data.repository.AuthRepository
import com.elsasa.btrade3.domain.model.ValidationResult

class ValidateSalesPersonUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): ValidationResult {
        return repository.isSalesPersonValid(email)
    }
}

