package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Agent (val name : String) {
    val intentions: Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

    //TODO change: all agents now start with an initial "goal" to achieve
    val events: MutableList<CompletableDeferred<Unit>> = mutableListOf(CompletableDeferred())

    val context = IntentionInterceptor(intentions) + CoroutineName(name)

    fun say(message: String){
        log("$name: $message")
    }

    // This fakes an action that awaits the achievement of a subgoal
    suspend fun achieve() {
        val deferred : CompletableDeferred<Unit> = CompletableDeferred()
        events.add(deferred)
        say("Waiting subgoal to complete...")
        deferred.await()
    }

    //This fakes matching a plan for a goal and pursuing it
    suspend fun launchPlan(completion: CompletableDeferred<Unit>){
        say("working towards goal")
        delay(1000);
        completion.complete(Unit)
        say("goal achieved!")
        achieve()
    }

    suspend fun run() = coroutineScope {
        say("Started...")
        while(true){
            //If there is a goal to be achieved, launch a plan for it
            if(events.isNotEmpty()){
                val deferred = events.removeFirst()
                launch(context){
                    launchPlan(deferred)
                }
            }
            //Execute one step of the next available intention, or wait indefinitely
            val continuation = intentions.receive()
            continuation()
        }
    }
}