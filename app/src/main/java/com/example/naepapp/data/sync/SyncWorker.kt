package com.example.naepapp.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.naepapp.BuildConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) {
            Log.d("NaepSync", "SyncWorker doWork invoked")
        }
        val result = syncEngine.drain()
        return if (result.isSuccess) Result.success() else Result.retry()
    }
}
