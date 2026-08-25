package com.nadekosu.domain.usecase

import com.nadekosu.data.packageinfo.SuperUserRepository

class GetSuperUserAppGroupUseCase(private val repository: SuperUserRepository) {
    suspend operator fun invoke(uid: Int, primaryPackageName: String) =
        repository.getAppGroup(uid, primaryPackageName)
}
