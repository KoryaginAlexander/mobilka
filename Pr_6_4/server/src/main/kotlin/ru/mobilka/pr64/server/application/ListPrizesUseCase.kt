package ru.mobilka.pr64.server.application

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.mobilka.pr64.server.data.NobelRemoteClient
import ru.mobilka.pr64.server.domain.repository.PrizeRepository
import ru.mobilka.pr64.server.presentation.dto.PrizesListResponse

class ListPrizesUseCase(
    private val prizes: PrizeRepository,
    private val remote: NobelRemoteClient,
    private val syncIntervalMs: Long,
) {
    private val syncMutex = Mutex()

    suspend operator fun invoke(refresh: Boolean): PrizesListResponse {
        val emptyCache = prizes.findAllDetailed().isEmpty()
        val stale = isStale()
        if (refresh || emptyCache || stale) {
            return syncMutex.withLock {
                val stillEmpty = prizes.findAllDetailed().isEmpty()
                val stillStale = isStale()
                if (!refresh && !stillEmpty && !stillStale) {
                    return@withLock PrizesListResponse(
                        prizes = prizes.findAllDetailed(),
                        syncedFromRemote = false,
                    )
                }
                val remoteData = remote.fetchAllNobelPrizes()
                prizes.upsertFromNobelApi(remoteData)
                prizes.markSyncedNow()
                PrizesListResponse(
                    prizes = prizes.findAllDetailed(),
                    syncedFromRemote = true,
                )
            }
        }

        return PrizesListResponse(
            prizes = prizes.findAllDetailed(),
            syncedFromRemote = false,
        )
    }

    private fun isStale(): Boolean {
        val last = prizes.lastRemoteSyncMillis() ?: return true
        return System.currentTimeMillis() - last > syncIntervalMs
    }
}
