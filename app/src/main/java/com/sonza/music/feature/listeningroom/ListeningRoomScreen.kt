package com.sonza.music.feature.listeningroom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.music.core.model.ListeningRoom
import com.sonza.music.core.model.RoomRole
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaEmerald
import com.sonza.music.core.theme.SonzaHiResBadgeBg
import com.sonza.music.core.theme.SonzaRose
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary
import com.sonza.music.data.repository.ListenTogetherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListeningRoomViewModel @Inject constructor(
    private val listenTogetherRepository: ListenTogetherRepository
) : ViewModel() {

    val activeRoom: StateFlow<ListeningRoom?> = listenTogetherRepository.activeRoomFlow
    val syncLatencyMs: StateFlow<Long> = listenTogetherRepository.syncLatencyMsFlow

    fun createRoom(title: String, name: String) {
        viewModelScope.launch {
            listenTogetherRepository.createRoom(title, name)
        }
    }

    fun joinRoom(code: String, name: String) {
        viewModelScope.launch {
            listenTogetherRepository.joinRoom(code, name)
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            listenTogetherRepository.leaveRoom()
        }
    }
}

@Composable
fun ListeningRoomScreen(
    activeRoom: ListeningRoom?,
    syncLatencyMs: Long,
    onCreateRoom: (String, String) -> Unit,
    onJoinRoom: (String, String) -> Unit,
    onLeaveRoom: () -> Unit,
    onDismiss: () -> Unit
) {
    var roomTitleInput by remember { mutableStateOf("Audiophile Master Session") }
    var userNameInput by remember { mutableStateOf("Prince") }
    var roomCodeInput by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SonzaTextPrimary)
                }

                Text(
                    text = "Listen Together",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary
                )

                if (activeRoom != null) {
                    Button(
                        onClick = onLeaveRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = SonzaRose),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Leave", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeRoom == null) {
                // Room Creation / Joining Form
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCreating) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isCreating = true }
                            ) {
                                Text(
                                    text = "Create Room",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCreating) SonzaCyanAccent else SonzaTextSecondary,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isCreating) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isCreating = false }
                            ) {
                                Text(
                                    text = "Join Room",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isCreating) SonzaCyanAccent else SonzaTextSecondary,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = { userNameInput = it },
                            label = { Text("Your Display Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonzaCyanAccent,
                                focusedTextColor = SonzaTextPrimary,
                                unfocusedTextColor = SonzaTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isCreating) {
                            OutlinedTextField(
                                value = roomTitleInput,
                                onValueChange = { roomTitleInput = it },
                                label = { Text("Room Session Title") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SonzaCyanAccent,
                                    focusedTextColor = SonzaTextPrimary,
                                    unfocusedTextColor = SonzaTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { onCreateRoom(roomTitleInput, userNameInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = SonzaCyanAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create Synchronized Room", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedTextField(
                                value = roomCodeInput,
                                onValueChange = { roomCodeInput = it.uppercase() },
                                label = { Text("6-Digit Room Code (e.g. SNZ-842)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SonzaCyanAccent,
                                    focusedTextColor = SonzaTextPrimary,
                                    unfocusedTextColor = SonzaTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { onJoinRoom(roomCodeInput, userNameInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = SonzaCyanAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Join Room", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Active Room Display
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeRoom.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaTextPrimary
                                )
                                Text(
                                    text = "Room Code: ${activeRoom.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SonzaCyanAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Live Drift / Latency Sync Indicator
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SonzaHiResBadgeBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = SonzaEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${syncLatencyMs}ms sync",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SonzaEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Connected Listeners (${activeRoom.members.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SonzaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeRoom.members) { member ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SonzaSurface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = SonzaCyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SonzaTextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (member.role == RoomRole.HOST) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = SonzaCyanAccent.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "HOST",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SonzaCyanAccent,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
