package api.environment

import api.event.Event
import kotlinx.coroutines.flow.Flow

interface Environment : Flow<Event.External>