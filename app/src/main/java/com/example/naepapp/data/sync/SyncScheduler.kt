package com.example.naepapp.data.sync

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.example.naepapp.core.common.ConnectivityMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context?,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val workManager = context?.let { WorkManager.getInstance(it) }

    init {
        if (context != null && workManager != null) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "naep-periodic-sync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )

            CoroutineScope(Dispatchers.IO).launch {
                connectivityMonitor.isOnline.collectLatest { online ->
                    if (online) {
                        triggerSync()
                    }
                }
            }

            try {
                ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) {
                        triggerSync()
                    }
                })
            } catch (_: Exception) {}
        }
    }

    open fun triggerSync() {
        val wm = workManager ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        wm.enqueueUniqueWork(
            "naep-sync",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }
}
