package agent

import kotlinx.coroutines.channels.Channel

interface Environment {
    val perceptions: Channel<PerceptionEvent<*>>
}