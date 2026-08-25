package com.nadekosu.domain.usecase

import com.nadekosu.data.flash.FlashRepository
import com.nadekosu.domain.model.FlashOperation

class ExecuteFlashOperationUseCase(private val repository: FlashRepository) {
    operator fun invoke(operation: FlashOperation) = repository.execute(operation)
}
