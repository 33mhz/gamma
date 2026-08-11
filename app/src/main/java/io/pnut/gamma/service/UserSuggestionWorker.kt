package io.pnut.gamma.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.data.db.entities.UserSuggestionEntity
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.util.LogUtil

@HiltWorker
class UserSuggestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pnutRepository: IPnutRepository,
    private val cacheDao: CacheDao,
    private val preferenceRepository: IPreferenceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        LogUtil.d("UserSuggestionWorker started")
        if (!preferenceRepository.usernameAutocomplete) {
            return Result.success()
        }
        return try {
            var beforeId: String? = null
            var hasMore = true
            val allSuggestions = mutableListOf<UserSuggestionEntity>()

            while (hasMore) {
                val params = GetUsersParam()
                params.add(PaginationParam(count = 200, beforeId = beforeId))
                
                val response = pnutRepository.searchUsers(params)
                val users = response.data

                if (users.isEmpty()) {
                    hasMore = false
                } else {
                    allSuggestions.addAll(users.map { 
                        UserSuggestionEntity(
                            id = it.id,
                            username = it.username,
                            name = it.name,
                            youFollow = it.youFollow
                        )
                    })
                    beforeId = response.meta.minId ?: users.last().paginationId ?: users.last().id

                    hasMore = (response.meta.more ?: false) && users.size >= 200
                }
                
                if (allSuggestions.size > 3000) break 
            }

            if (allSuggestions.isNotEmpty()) {
                cacheDao.insertSuggestions(allSuggestions)
            }
            Result.success()
        } catch (e: Exception) {
            LogUtil.e("UserSuggestionWorker error: ${e.message}")
            Result.retry()
        }
    }
}
