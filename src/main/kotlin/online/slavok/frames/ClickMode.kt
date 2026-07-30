package online.slavok.frames

/** Which mouse button triggers an interaction: left-click, right-click, or both. */
enum class ClickMode {
    LEFT, RIGHT, BOTH;

    fun allowsLeft(): Boolean = this == LEFT || this == BOTH
    fun allowsRight(): Boolean = this == RIGHT || this == BOTH

    companion object {
        /** Case-insensitive parse; any unknown/blank value falls back to [default]. */
        @JvmStatic
        fun parse(value: String?, default: ClickMode): ClickMode =
            value?.trim()?.uppercase()?.let { runCatching { valueOf(it) }.getOrNull() } ?: default
    }
}
