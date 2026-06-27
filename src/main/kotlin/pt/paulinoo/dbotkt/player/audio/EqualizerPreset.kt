package pt.paulinoo.dbotkt.player.audio

/**
 * Equalizer presets backed by Lavaplayer's native 15-band PCM equalizer.
 *
 * Each preset holds one gain per band (15 total), in the band order used by
 * Lavaplayer: 25, 40, 63, 100, 160, 250, 400, 630, 1000, 1600, 2500, 4000,
 * 6300, 10000 and 16000 Hz. Valid gains range from -0.25 to +1.0; values are
 * kept moderate on purpose to boost/cut audibly without introducing clipping,
 * preserving the maximum output quality.
 *
 * [FLAT] is a no-op: the audio manager bypasses the filter entirely for it, so
 * the default playback path has zero added CPU cost.
 */
enum class EqualizerPreset(
    val displayName: String,
    val emoji: String,
    val gains: FloatArray,
) {
    FLAT(
        "Flat",
        "U+1F3B5",
        floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    ),
    BASS_BOOST(
        "Bass Boost",
        "U+1F50A",
        floatArrayOf(0.22f, 0.20f, 0.18f, 0.14f, 0.08f, 0.02f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    ),
    TREBLE_BOOST(
        "Treble Boost",
        "U+2728",
        floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.04f, 0.07f, 0.10f, 0.14f, 0.18f, 0.20f, 0.22f),
    ),
    POP(
        "Pop",
        "U+1F3A4",
        floatArrayOf(-0.02f, -0.01f, 0f, 0.02f, 0.06f, 0.09f, 0.08f, 0.05f, 0.01f, -0.01f, -0.01f, 0.01f, 0.03f, 0.04f, 0.05f),
    ),
    ROCK(
        "Rock",
        "U+1F3B8",
        floatArrayOf(0.12f, 0.09f, 0.06f, 0.03f, 0f, -0.03f, -0.05f, -0.02f, 0.01f, 0.04f, 0.07f, 0.09f, 0.11f, 0.12f, 0.13f),
    ),
    JAZZ(
        "Jazz",
        "U+1F3B7",
        floatArrayOf(0.07f, 0.05f, 0.03f, 0.02f, 0.01f, -0.01f, -0.02f, -0.01f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.05f, 0.04f),
    ),
    CLASSICAL(
        "Classical",
        "U+1F3BB",
        floatArrayOf(0.06f, 0.05f, 0.04f, 0.03f, 0.01f, 0f, 0f, 0f, 0f, -0.01f, -0.02f, -0.03f, -0.04f, -0.05f, -0.06f),
    ),
    LOUDNESS(
        "Loudness",
        "U+1F525",
        floatArrayOf(0.15f, 0.12f, 0.08f, 0.04f, 0f, -0.03f, -0.05f, -0.03f, 0f, 0.04f, 0.08f, 0.11f, 0.13f, 0.14f, 0.15f),
    ),
    ;

    /** Stable lowercase identifier used in component ids and text commands. */
    val id: String get() = name.lowercase()

    companion object {
        fun fromId(value: String): EqualizerPreset? {
            val normalized = value.trim().lowercase()
            return entries.firstOrNull {
                it.id == normalized || it.displayName.equals(value.trim(), ignoreCase = true)
            }
        }
    }
}
