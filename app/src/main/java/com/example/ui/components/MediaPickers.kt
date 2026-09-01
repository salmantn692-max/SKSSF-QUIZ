package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.AudioPlaybackState
import com.example.audio.QuizAudioPlayer

data class ImagePreset(
    val key: String,
    val label: String,
    val imageUrl: String,
    val description: String
)

data class AudioPreset(
    val key: String,
    val label: String,
    val description: String,
    val synthTag: String
)

val SAMPLE_IMAGE_PRESETS = listOf(
    ImagePreset(
        key = "preset:telescope",
        label = "ജെയിംസ് വെബ് സ്പേസ് ടെലിസ്കോപ്പ്",
        imageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=600&auto=format&fit=crop&q=80",
        description = "സ്വർണ്ണ ഷഡ്ഭുജ ദർപ്പണങ്ങളുള്ള ഇൻഫ്രാറെഡ് ഒബ്സർവേറ്ററി"
    ),
    ImagePreset(
        key = "preset:ashram",
        label = "സബർമതി ആശ്രമം പൈതൃകം",
        imageUrl = "https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?w=600&auto=format&fit=crop&q=80",
        description = "ഗാന്ധിജിയുടെ സബർമതി നദീതീരത്തെ ആശ്രമം"
    ),
    ImagePreset(
        key = "preset:cern",
        label = "സേൺ കണികാ പരീക്ഷണശാല (CERN)",
        imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=600&auto=format&fit=crop&q=80",
        description = "ലാർജ് ഹാഡ്രോൺ കൊളൈഡർ ഡിറ്റക്ടർ"
    ),
    ImagePreset(
        key = "preset:microscope",
        label = "ഇലക്ട്രോൺ മൈക്രോസ്കോപ്പ്",
        imageUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=600&auto=format&fit=crop&q=80",
        description = "നാനോടെക് കോശ ഘടനകൾ"
    ),
    ImagePreset(
        key = "preset:solar",
        label = "സൂര്യഗ്രഹണ കൊറോണ",
        imageUrl = "https://images.unsplash.com/photo-1538370965046-79c0d6907d47?w=600&auto=format&fit=crop&q=80",
        description = "സൂര്യന്റെ പ്രഭാവലയ ദൃശ്യം"
    )
)

val SAMPLE_AUDIO_PRESETS = listOf(
    AudioPreset(
        key = "preset:morse",
        label = "ടെലിഗ്രാഫിക് മോഴ്സ് SOS കോഡ്",
        description = "800 Hz റേഡിയോ ടോൺ സന്ദേശം",
        synthTag = "synth:morse"
    ),
    AudioPreset(
        key = "preset:melody",
        label = "പെന്റാറ്റോണിക് ഹാർമോണിക് മണിനാദം",
        description = "ആരോഹണ ക്രമത്തിലുള്ള പ്യുവർ ടോൺ സംഗീതം",
        synthTag = "synth:melody"
    ),
    AudioPreset(
        key = "preset:sonar",
        label = "ആക്ടീവ് സോണാർ പിംഗ് (Sonar)",
        description = "അന്തർവാഹിനി ശബ്ദതരംഗ സ്പന്ദനം",
        synthTag = "synth:sonar"
    ),
    AudioPreset(
        key = "preset:bell",
        label = "ക്ഷേത്ര മണിനാദം / റെസൊണൻസ് ബെൽ",
        description = "ആഴമേറിയ ഹാർമോണിക് നാദം",
        synthTag = "synth:bell"
    ),
    AudioPreset(
        key = "preset:space",
        label = "ബഹിരാകാശ ഉപഗ്രഹ ടെലിമെട്രി",
        description = "പൾസിംഗ് കോസ്മിക് ഫ്രീക്വൻസി സിഗ്നൽ",
        synthTag = "synth:space"
    ),
    AudioPreset(
        key = "preset:gandhi_speech_cue",
        label = "ചരിത്ര പ്രഖ്യാപന ഫാൻഫെയർ",
        description = "രാജകീയ ട്രമ്പറ്റ് സംഗീത തുടക്കം",
        synthTag = "synth:speech_cue"
    )
)

@Composable
fun ImageMediaSelector(
    selectedMediaUri: String,
    onMediaSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPresetDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri.toString())
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "റൗണ്ട് 2 ചിത്ര ക്ലൂ മീഡിയ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (selectedMediaUri.isNotBlank()) {
            val preset = SAMPLE_IMAGE_PRESETS.find { it.key == selectedMediaUri }
            val displayModel = if (preset != null) preset.imageUrl else selectedMediaUri

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(displayModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "തിരഞ്ഞെടുത്ത ചോദ്യ ചിത്രം",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = preset?.label ?: "തിരഞ്ഞെടുത്ത ചിത്രം",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { onMediaSelected("") },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "ചിത്രം നീക്കം ചെയ്യുക",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ചിത്രം തിരഞ്ഞെടുക്കൂ", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { showPresetDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("മാതൃകാ ചിത്രങ്ങൾ", fontSize = 12.sp)
            }
        }
    }

    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("ചിത്ര ക്ലൂ തിരഞ്ഞെടുക്കുക") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SAMPLE_IMAGE_PRESETS.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedMediaUri == preset.key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMediaSelected(preset.key)
                                    showPresetDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = preset.imageUrl,
                                    contentDescription = preset.label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                    )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("റദ്ദാക്കുക")
                }
            }
        )
    }
}

@Composable
fun AudioMediaSelector(
    selectedMediaUri: String,
    audioPlayer: QuizAudioPlayer,
    onMediaSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPresetDialog by remember { mutableStateOf(false) }
    val playbackState by audioPlayer.playbackState.collectAsState()

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri.toString())
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "റൗണ്ട് 3 ഓഡിയോ ക്ലൂ മീഡിയ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (selectedMediaUri.isNotBlank()) {
            val preset = SAMPLE_AUDIO_PRESETS.find { it.key == selectedMediaUri }
            val isThisPlaying = playbackState.isPlaying && playbackState.currentAudioId == selectedMediaUri

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (isThisPlaying) {
                                    audioPlayer.stop()
                                } else {
                                    audioPlayer.playAudio(selectedMediaUri, preset?.label ?: "ഓഡിയോ ക്ലൂ")
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "കേൾക്കൂ / നിർത്തൂ",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset?.label ?: "ഓഡിയോ ഫയൽ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isThisPlaying) "ശബ്ദം കേൾക്കുന്നു..." else (preset?.description ?: "കേൾക്കാൻ തയ്യാറാണ്"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = {
                                audioPlayer.stop()
                                onMediaSelected("")
                            }
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "ഓഡിയോ മാറ്റുക",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (isThisPlaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { playbackState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    audioFilePickerLauncher.launch("audio/*")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ഓഡിയോ തിരഞ്ഞെടുക്കൂ", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { showPresetDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("മാതൃകാ ശബ്ദരേഖകൾ", fontSize = 12.sp)
            }
        }
    }

    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("ഓഡിയോ ക്ലൂ തിരഞ്ഞെടുക്കുക") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SAMPLE_AUDIO_PRESETS.forEach { preset ->
                        val isPlayingThis = playbackState.isPlaying && playbackState.currentAudioId == preset.key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedMediaUri == preset.key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMediaSelected(preset.key)
                                    showPresetDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isPlayingThis) {
                                            audioPlayer.stop()
                                        } else {
                                            audioPlayer.playAudio(preset.key, preset.label)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "ശബ്ദം പരിശോധിക്കുക",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("ശരി")
                }
            }
        )
    }
}
