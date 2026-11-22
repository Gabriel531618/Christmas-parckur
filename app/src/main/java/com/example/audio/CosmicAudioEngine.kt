package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AbilityType
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class CosmicAudioEngine {
    enum class Wave { SINE, TRIANGLE, SQUARE, SAWTOOTH }

    @Volatile
    var muted: Boolean = false

    private val executor = Executors.newCachedThreadPool()
    private val sampleRate = 22050

    fun playJump(isDouble: Boolean = false) {
        beep(
            freq = if (isDouble) 520f else 360f,
            wave = if (isDouble) Wave.TRIANGLE else Wave.SINE,
            durSec = 0.16f,
            vol = 0.16f,
            endFreq = if (isDouble) 960f else 720f
        )
    }

    fun playCheckpoint() {
        beep(784f, Wave.TRIANGLE, 0.08f, 0.17f)
        beep(1046.5f, Wave.TRIANGLE, 0.09f, 0.18f, delayMs = 65L)
        beep(1318.5f, Wave.SINE, 0.22f, 0.19f, delayMs = 135L)
    }

    fun playDoorPortal() {
        beep(440f, Wave.SINE, 0.12f, 0.17f, 660f)
        beep(659.25f, Wave.TRIANGLE, 0.14f, 0.18f, 987.77f, delayMs = 75L)
        beep(987.77f, Wave.SINE, 0.26f, 0.19f, 1480f, delayMs = 160L)
    }

    fun playGiftCollect() {
        beep(1046.5f, Wave.TRIANGLE, 0.07f, 0.16f)
        beep(1318.5f, Wave.TRIANGLE, 0.08f, 0.17f, delayMs = 55L)
        beep(1567.98f, Wave.SINE, 0.18f, 0.18f, delayMs = 115L)
    }

    fun playSantaSleigh() {
        beep(659.25f, Wave.SINE, 0.09f, 0.18f)
        beep(783.99f, Wave.TRIANGLE, 0.09f, 0.18f, delayMs = 70L)
        beep(1046.5f, Wave.SINE, 0.22f, 0.20f, delayMs = 140L)
    }

    fun playBounce() {
        beep(240f, Wave.SINE, 0.28f, 0.22f, 1020f)
    }

    fun playCoin() {
        beep(988f, Wave.SINE, 0.07f, 0.14f)
        beep(1319f, Wave.SINE, 0.14f, 0.14f, delayMs = 55L)
    }

    fun playDiamond() {
        beep(1046f, Wave.TRIANGLE, 0.07f, 0.16f)
        beep(1568f, Wave.TRIANGLE, 0.18f, 0.16f, delayMs = 75L)
    }

    fun playGalactic() {
        beep(880f, Wave.SINE, 0.08f, 0.17f)
        beep(1175f, Wave.TRIANGLE, 0.09f, 0.17f, delayMs = 60L)
        beep(1760f, Wave.SINE, 0.22f, 0.19f, delayMs = 120L)
    }

    fun playStomp() {
        beep(460f, Wave.SQUARE, 0.08f, 0.14f, 840f)
        beep(920f, Wave.SINE, 0.12f, 0.15f, delayMs = 65L)
    }

    fun playHurt() {
        beep(230f, Wave.SAWTOOTH, 0.28f, 0.16f, 68f)
    }

    fun playClick() {
        beep(600f, Wave.SQUARE, 0.06f, 0.11f, 880f)
    }

    fun playWin() {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.5f)
        notes.forEachIndexed { idx, f ->
            beep(f, Wave.TRIANGLE, 0.20f, 0.17f, delayMs = idx * 85L)
        }
    }

    fun playAbility(kind: AbilityType) {
        when (kind) {
            AbilityType.DASH -> beep(760f, Wave.SAWTOOTH, 0.20f, 0.20f, 1580f)
            AbilityType.SLOWMO -> beep(580f, Wave.SINE, 0.35f, 0.20f, 210f)
            AbilityType.SOLAR -> beep(360f, Wave.TRIANGLE, 0.28f, 0.22f, 960f)
            AbilityType.ZEROG -> beep(420f, Wave.SINE, 0.30f, 0.20f, 850f)
            AbilityType.BLIZZARD -> {
                beep(920f, Wave.SINE, 0.16f, 0.19f, 340f)
                beep(1240f, Wave.TRIANGLE, 0.22f, 0.17f, 620f, delayMs = 70L)
            }
            AbilityType.AURORA_NOVA -> {
                beep(523.25f, Wave.SINE, 0.14f, 0.18f, 1046.5f)
                beep(783.99f, Wave.TRIANGLE, 0.24f, 0.19f, 1567.98f, delayMs = 75L)
            }
            AbilityType.PRESENT_BARRAGE -> {
                beep(440f, Wave.TRIANGLE, 0.10f, 0.18f, 880f)
                beep(660f, Wave.SQUARE, 0.12f, 0.16f, 1320f, delayMs = 60L)
                beep(988f, Wave.SINE, 0.18f, 0.18f, 1480f, delayMs = 120L)
            }
            AbilityType.CANDY_TURBO -> {
                beep(620f, Wave.SAWTOOTH, 0.14f, 0.18f, 1280f)
                beep(880f, Wave.TRIANGLE, 0.22f, 0.20f, 1760f, delayMs = 65L)
            }
        }
    }

    private fun beep(
        freq: Float,
        wave: Wave,
        durSec: Float,
        vol: Float,
        endFreq: Float? = null,
        delayMs: Long = 0L
    ) {
        if (muted) return
        executor.execute {
            try {
                if (delayMs > 0L) Thread.sleep(delayMs)
                if (muted) return@execute

                val totalSamples = (sampleRate * durSec).toInt().coerceAtLeast(1)
                val buffer = ShortArray(totalSamples)
                var phase = 0.0

                for (i in 0 until totalSamples) {
                    val progress = i.toFloat() / totalSamples
                    val curFreq = if (endFreq != null) {
                        freq * (endFreq / freq).toDouble().pow(progress.toDouble()).toFloat()
                    } else {
                        freq
                    }
                    phase += 2.0 * PI * curFreq / sampleRate
                    val normPhase = ((phase / (2.0 * PI)) % 1.0 + 1.0) % 1.0

                    val rawSample = when (wave) {
                        Wave.SINE -> sin(phase)
                        Wave.TRIANGLE -> 1.0 - 4.0 * kotlin.math.abs(normPhase - 0.5)
                        Wave.SQUARE -> if (normPhase < 0.5) 1.0 else -1.0
                        Wave.SAWTOOTH -> 2.0 * normPhase - 1.0
                    }

                    // Smooth exponential fade-out envelope
                    val env = (1.0 - progress).pow(1.4)
                    val value = (rawSample * vol * env * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = value.toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                Thread.sleep((durSec * 1000).toLong() + 40L)
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
        }
    }
}
