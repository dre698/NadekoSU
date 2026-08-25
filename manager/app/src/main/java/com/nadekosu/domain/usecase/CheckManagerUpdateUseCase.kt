package com.nadekosu.domain.usecase

import com.nadekosu.data.update.ManagerUpdateRepository
import com.nadekosu.domain.model.ManagerUpdateChannel
import com.nadekosu.domain.model.ManagerUpdateInfo

class CheckManagerUpdateUseCase(
    private val repository: ManagerUpdateRepository,
) {
    suspend operator fun invoke(channel: ManagerUpdateChannel): ManagerUpdateInfo? =
        when (channel) {
            ManagerUpdateChannel.STABLE -> repository.checkStableUpdate()
            ManagerUpdateChannel.BETA -> repository.checkBetaUpdate()
        }
}
