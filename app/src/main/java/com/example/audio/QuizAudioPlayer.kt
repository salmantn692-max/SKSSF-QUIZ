package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentAudioId: String? = null,
    val progress: Float = 0f,
    val title: String = ""
)

class QuizAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun playAudio(audioKeyOrUri: String, title: String = "Audio Clue") {
        stop()

        if (audioKeyOrUri.startsWith("synth:") || audioKeyOrUri.startsWith("preset:")) {
            playSynthesizedPreset(audioKeyOrUri, title)
        } else if (audioKeyOrUri.startsWith("content://") || audioKeyOrUri.startsWith("file://") || audioKeyOrUri.startsWith("http")) {
            playUri(audioKeyOrUri, title)
        } else {
            // Default fallback to synthesized tone if unknown
            playSynthesizedPreset("preset:sonar", title)
        }
    }

    private fun playUri(uriString: String, title: String) {
        try {
            val uri = Uri.parse(uriString)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    mp.start()
                    _playbackState.value = AudioPlaybackState(
                        isPlaying = true,
                        currentAudioId = uriString,
                        progress = 0f,
                        title = title
                    )
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuizAudioPlayer", "Error playing audio $what, $extra")
                    stop()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("QuizAudioPlayer", "Failed to initialize media player", e)
            stop()
        }
    }

    private fun startProgressTracker() {
        scope.launch {
            while (mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val total = mediaPlayer?.duration ?: 1
                val prog = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                _playbackState.value = _playbackState.value.copy(progress = prog)
                delay(100)
            }
        }
    }

    private fun playSynthesizedPreset(presetKey: String, title: String) {
        synthJob?.cancel()
        synthJob = scope.launch {
            _playbackState.value = AudioPlaybackState(
                isPlaying = true,
                currentAudioId = presetKey,
                progress = 0f,
                title = title
            )

            try {
                when (presetKey) {
                    "preset:morse", "synth:morse" -> playMorseTone()
                    "preset:melody", "synth:melody" -> playChimeMelody()
                    "preset:bell", "synth:bell" -> playGongChime()
                    "preset:sonar", "synth:sonar" -> playSonarPing()
                    "preset:space", "synth:space" -> playSpacePulse()
                    "preset:gandhi_speech_cue", "synth:speech_cue" -> playSpeechFanfare()
                    else -> playChimeMelody()
                }
            } catch (e: Exception) {
                Log.e("QuizAudioPlayer", "Synth error", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _playbackState.value = AudioPlaybackState(isPlaying = false, currentAudioId = null, progress = 1f)
                }
            }
        }
    }

    private suspend fun playTone(freqHz: Double, durationMs: Int, sampleRate: Int = 44100) {
        val numSamples = (durationMs * sampleRate / 1000)
        val generatedSnd = ShortArray(numSamples)
        val phaseIncrement = (2.0 * Math.PI * freqHz) / sampleRate
        var phase = 0.0

        for (i in 0 until numSamples) {
            // Apply simple attack/decay envelope to prevent clicking
            val envelope = when {
                i < 500 -> (i.toDouble() / 500.0)
                i > numSamples - 500 -> ((numSamples - i).toDouble() / 500.0)
                else -> 1.0
            }
            generatedSnd[i] = (sin(phase) * 32767 * 0.7 * envelope).toInt().toShort()
            phase += phaseIncrement
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize.coerceAtLeast(numSamples * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, numSamples)
        audioTrack.play()
        delay(durationMs.toLong())
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            // ignore
        }
    }

    private suspend fun playMorseTone() {
        // Morse S-O-S pattern: ... --- ...
        val dot = 100
        val dash = 300
        val f = 800.0
        val dots = listOf(dot, dot, dot, dash, dash, dash, dot, dot, dot)
        dots.forEachIndexed { idx, d ->
            playTone(f, d)
            delay(80)
            _playbackState.value = _playbackState.value.copy(progress = (idx + 1).toFloat() / dots.size)
        }
    }

    private suspend fun playChimeMelody() {
        // Classical Pentatonic Chime: C5, E5, G5, B5, C6, G5, E5, C5
        val notes = listOf(523.25, 659.25, 783.99, 987.77, 1046.50, 783.99, 659.25, 523.25)
        notes.forEachIndexed { idx, freq ->
            playTone(freq, 220)
            delay(40)
            _playbackState.value = _playbackState.value.copy(progress = (idx + 1).toFloat() / notes.size)
        }
    }

    private suspend fun playGongChime() {
        // Deep resonance: 220Hz, 330Hz, 440Hz
        val chords = listOf(220.0, 329.63, 440.0, 554.37)
        chords.forEachIndexed { idx, freq ->
            playTone(freq, 350)
            delay(60)
            _playbackState.value = _playbackState.value.copy(progress = (idx + 1).toFloat() / chords.size)
        }
    }

    private suspend fun playSonarPing() {
        // High ping and sweeping pulse
        for (i in 1..4) {
            playTone(1200.0 + (i * 150), 180)
            delay(200)
            _playbackState.value = _playbackState.value.copy(progress = i / 4f)
        }
    }

    private suspend fun playSpacePulse() {
        val sweep = listOf(300.0, 450.0, 600.0, 900.0, 1200.0, 800.0, 400.0)
        sweep.forEachIndexed { idx, freq ->
            playTone(freq, 160)
            delay(30)
            _playbackState.value = _playbackState.value.copy(progress = (idx + 1).toFloat() / sweep.size)
        }
    }

    private suspend fun playSpeechFanfare() {
        // Royal Brass fanfare intro
        val fanfare = listOf(440.0, 440.0, 440.0, 554.37, 659.25, 880.0)
        fanfare.forEachIndexed { idx, freq ->
            playTone(freq, if (idx == fanfare.size - 1) 450 else 160)
            delay(50)
            _playbackState.value = _playbackState.value.copy(progress = (idx + 1).toFloat() / fanfare.size)
        }
    }

    fun stop() {
        synthJob?.cancel()
        synthJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState(isPlaying = false, currentAudioId = null, progress = 0f)
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
