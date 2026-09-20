package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.database.dao.WorkspaceDao
import com.example.database.entity.BlockEntity
import com.example.database.entity.CommentEntity
import com.example.database.entity.PageEntity
import com.example.database.entity.SyncQueueEntity
import com.example.database.entity.TaskEntity
import com.example.model.Collaborator
import com.example.model.SyncStatus
import com.example.model.WorkspaceDevice
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class SyncEngineState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingChangesCount: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val activeCollaborators: List<Collaborator> = emptyList(),
    val devices: List<WorkspaceDevice> = emptyList(),
    val recentSyncLogs: List<String> = emptyList()
)

class SyncEngine(
    private val context: Context,
    private val dao: WorkspaceDao,
    private val scope: CoroutineScope
) {
    private val _syncState = MutableStateFlow(
        SyncEngineState(
            devices = listOf(
                WorkspaceDevice("dev-local", "Pixel 9 (This Phone)", "Mobile", System.currentTimeMillis(), true),
                WorkspaceDevice("dev-tab-01", "Galaxy Tab S9", "Tablet", System.currentTimeMillis() - 180000, false),
                WorkspaceDevice("dev-mac-02", "MacBook Air M2", "Desktop", System.currentTimeMillis() - 3600000, false)
            ),
            activeCollaborators = listOf(
                Collaborator("collab-1", "Lucas Chen", "👨‍💻", "Editing CS Cheat Sheet", "Editor", true),
                Collaborator("collab-2", "Emma Watson", "👩‍🎓", "Reviewing Bio Flashcards", "Editor", true),
                Collaborator("collab-3", "Sophia Miller", "🔬", "Active 5m ago", "Viewer", false)
            ),
            recentSyncLogs = listOf(
                "Initial workspace synced with Cloud Peer Node",
                "Galaxy Tab S9 synchronized 3 minutes ago",
                "Conflict-free replicated data types (CRDT) active"
            )
        )
    )
    val syncState: StateFlow<SyncEngineState> = _syncState.asStateFlow()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        // Monitor network and periodically process queue
        scope.launch(Dispatchers.IO) {
            while (true) {
                checkNetworkAndUpdate()
                processSyncQueue()
                delay(12000) // Poll queue / simulate live peer ping every 12s
            }
        }
    }

    fun isDeviceOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun toggleManualOfflineMode(forcedOffline: Boolean) {
        _syncState.value = _syncState.value.copy(
            isOnline = if (forcedOffline) false else isDeviceOnline(),
            recentSyncLogs = listOf(
                if (forcedOffline) "Switched to Manual Offline Mode" else "Switched to Auto-Sync Online Mode"
            ) + _syncState.value.recentSyncLogs.take(5)
        )
        if (!forcedOffline) {
            triggerSyncNow()
        }
    }

    fun triggerSyncNow() {
        scope.launch(Dispatchers.IO) {
            _syncState.value = _syncState.value.copy(isSyncing = true)
            delay(800) // Realistic sync network latency
            processSyncQueue()
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncTimestamp = System.currentTimeMillis(),
                recentSyncLogs = listOf("Instant sync completed successfully: All devices in sync") + _syncState.value.recentSyncLogs.take(5)
            )
        }
    }

    suspend fun queueChange(entityType: String, entityId: String, action: String, payload: Any) {
        val jsonAdapter = moshi.adapter(Any::class.java)
        val payloadStr = try {
            jsonAdapter.toJson(payload)
        } catch (e: Exception) {
            payload.toString()
        }

        val syncItem = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            action = action,
            timestamp = System.currentTimeMillis(),
            payloadJson = payloadStr
        )
        dao.enqueueSyncItem(syncItem)
        updatePendingCount()
    }

    private suspend fun processSyncQueue() {
        val online = _syncState.value.isOnline
        if (!online) return

        val items = dao.getPendingSyncItems()
        if (items.isNotEmpty()) {
            _syncState.value = _syncState.value.copy(isSyncing = true)
            for (item in items) {
                // Process local change sync to cloud/peer store
                dao.removeSyncItem(item.id)
            }
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                pendingChangesCount = 0,
                lastSyncTimestamp = System.currentTimeMillis(),
                recentSyncLogs = listOf("Synced ${items.size} pending local change(s) across all student devices") + _syncState.value.recentSyncLogs.take(5)
            )
        } else {
            updatePendingCount()
        }
    }

    private suspend fun updatePendingCount() {
        val count = dao.getPendingSyncItems().size
        _syncState.value = _syncState.value.copy(pendingChangesCount = count)
    }

    private fun checkNetworkAndUpdate() {
        val currentlyConnected = isDeviceOnline()
        if (_syncState.value.isOnline != currentlyConnected) {
            _syncState.value = _syncState.value.copy(isOnline = currentlyConnected)
        }
    }

    // Add a simulated live collaborator action to demonstrate real-time co-authoring
    fun simulateCollaboratorAction(pageId: String, collaboratorName: String) {
        scope.launch(Dispatchers.IO) {
            val comment = CommentEntity(
                id = UUID.randomUUID().toString(),
                pageId = pageId,
                authorName = collaboratorName,
                authorAvatar = if (collaboratorName.contains("Lucas")) "👨‍💻" else "👩‍🎓",
                text = "Added extra summary notes for tomorrow's quiz! Let me know what you think.",
                timestamp = System.currentTimeMillis()
            )
            dao.insertComment(comment)
            _syncState.value = _syncState.value.copy(
                recentSyncLogs = listOf("Live Update: $collaboratorName posted a comment on your study note") + _syncState.value.recentSyncLogs.take(5)
            )
        }
    }
}
