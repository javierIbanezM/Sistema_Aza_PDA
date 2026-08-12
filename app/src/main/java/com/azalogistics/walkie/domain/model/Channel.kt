package com.azalogistics.walkie.domain.model

/**
 * A logical voice channel. Each channel deterministically maps to a UDP port so any
 * device that "knows" the channel id opens the same port without a server. Membership
 * (who's actually on the channel right now) is unicast presence, not a multicast group —
 * see NetworkManager.
 */
data class Channel(
    val id: String,        // stable, human-shareable, e.g. "ops" or "team-3"
    val name: String,
) {
    val port: Int
        get() = BASE_PORT + (stableHash(id) and 0x0FFF) // 50000..54095

    companion object {
        const val BASE_PORT = 50000
        private fun stableHash(s: String): Int {
            var h = 2166136261.toInt()
            for (c in s) { h = h xor c.code; h *= 16777619 }
            return h and 0x7FFFFFFF
        }
    }
}
