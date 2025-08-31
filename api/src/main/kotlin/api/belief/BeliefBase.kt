package api.belief

import api.event.Event
import kotlinx.coroutines.flow.Flow

interface BeliefBase<Belief : Any> :
    Set<Belief>,
    Flow<Event.Internal.Belief<Belief>>