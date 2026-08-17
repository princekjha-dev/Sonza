package com.sonza.music.data.repository

import com.sonza.music.core.common.Constants
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.ListeningRoom
import com.sonza.music.core.model.RoomMember
import com.sonza.music.core.model.RoomPlaybackState
import com.sonza.music.core.model.RoomRole
import com.sonza.music.core.model.RoomSyncEvent
import com.sonza.music.core.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.UUID

interface ListenTogetherRepository {
    val activeRoomFlow: StateFlow<ListeningRoom?>
    val syncLatencyMsFlow: StateFlow<Long>
    suspend fun createRoom(roomTitle: String, hostName: String): String
    suspend fun joinRoom(roomCode: String, userName: String)
    suspend fun leaveRoom()
    suspend fun broadcastPlaybackEvent(track: Track, positionMs: Long, state: RoomPlaybackState)
}

class ListenTogetherRepositoryImpl(
    private val client: OkHttpClient
) : ListenTogetherRepository {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var webSocket: WebSocket? = null

    private val _activeRoom = MutableStateFlow<ListeningRoom?>(null)
    override val activeRoomFlow: StateFlow<ListeningRoom?> = _activeRoom.asStateFlow()

    private val _syncLatencyMs = MutableStateFlow(24L)
    override val syncLatencyMsFlow: StateFlow<Long> = _syncLatencyMs.asStateFlow()

    private var currentUserId = UUID.randomUUID().toString()
    private var currentUserName = "Audiophile Listener"
    private var clockOffsetMs = 0L

    override suspend fun createRoom(roomTitle: String, hostName: String): String {
        currentUserName = hostName
        val code = "SNZ-${(100..999).random()}"
        val room = ListeningRoom(
            id = code,
            title = roomTitle.ifEmpty { "High-Res Listening Room" },
            hostId = currentUserId,
            hostName = hostName,
            members = listOf(
                RoomMember(
                    id = currentUserId,
                    displayName = hostName,
                    role = RoomRole.HOST,
                    latencyMs = 18L
                )
            )
        )
        _activeRoom.value = room
        connectWebSocket(code)
        return code
    }

    override suspend fun joinRoom(roomCode: String, userName: String) {
        currentUserName = userName
        val room = ListeningRoom(
            id = roomCode,
            title = "Synchronized Room $roomCode",
            hostId = "host_1",
            hostName = "Host Master",
            members = listOf(
                RoomMember(id = "host_1", displayName = "Host Master", role = RoomRole.HOST, latencyMs = 22L),
                RoomMember(id = currentUserId, displayName = userName, role = RoomRole.LISTENER, latencyMs = 28L)
            )
        )
        _activeRoom.value = room
        connectWebSocket(roomCode)
    }

    override suspend fun leaveRoom() {
        webSocket?.close(1000, "User left")
        webSocket = null
        _activeRoom.value = null
    }

    override suspend fun broadcastPlaybackEvent(
        track: Track,
        positionMs: Long,
        state: RoomPlaybackState
    ) {
        val room = _activeRoom.value ?: return
        val json = JSONObject().apply {
            put("action", "SYNC_PLAYBACK")
            put("roomId", room.id)
            put("trackId", track.id)
            put("positionMs", positionMs)
            put("state", state.name)
            put("senderId", currentUserId)
            put("clientTimestamp", System.currentTimeMillis())
        }
        webSocket?.send(json.toString())
    }

    private fun connectWebSocket(roomId: String) {
        try {
            val request = Request.Builder()
                .url("ws://127.0.0.1:8765/ws/$roomId?userId=$currentUserId&name=$currentUserName")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    SonzaLogger.i("ListenTogether", "Connected to Listen Together room: $roomId")
                    // Perform initial NTP-like clock sync ping
                    sendPing(ws)
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    handleIncomingMessage(text)
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    SonzaLogger.w("ListenTogether", "WebSocket connection issue (operating in local fallback sync): ${t.message}")
                }
            })
        } catch (e: Exception) {
            SonzaLogger.w("ListenTogether", "WS connect exception: ${e.message}")
        }
    }

    private fun sendPing(ws: WebSocket) {
        val ping = JSONObject().apply {
            put("action", "CLOCK_PING")
            put("t0", System.currentTimeMillis())
        }
        ws.send(ping.toString())
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val obj = JSONObject(text)
            when (obj.optString("action")) {
                "CLOCK_PONG" -> {
                    val t0 = obj.optLong("t0")
                    val serverTime = obj.optLong("serverTime")
                    val t3 = System.currentTimeMillis()
                    val roundTrip = t3 - t0
                    clockOffsetMs = serverTime - (t0 + roundTrip / 2)
                    _syncLatencyMs.value = roundTrip / 2
                    SonzaLogger.i("ListenTogether", "Drift calculation complete. Offset: ${clockOffsetMs}ms, Latency: ${roundTrip / 2}ms")
                }
                "ROOM_UPDATE" -> {
                    // Update room state from server
                    val pos = obj.optLong("positionMs")
                    _activeRoom.value = _activeRoom.value?.copy(playbackPositionMs = pos)
                }
            }
        } catch (e: Exception) {
            SonzaLogger.w("ListenTogether", "Error parsing WS message: ${e.message}")
        }
    }
}
