package com.matteblack.fluent


interface State
{
    val transitions: List<Transition>
    val name: String

    fun onStart()
    fun onLoop()
    fun onExit()
}

class StateBuilder {
    var transitionsVar = mutableListOf<Transition>()

    private var startFunction: () -> Unit = {}
    private var loopFunction: () -> Unit = {}
    private var exitFunction: () -> Unit = {}
    private lateinit var stateName: () -> String

    fun called(func: () -> String) {
        stateName = func
    }

    fun start(func: () -> Unit) {
        startFunction = func
    }
    fun loop(func: () -> Unit) {
        loopFunction = func
    }
    fun exit(func: () -> Unit) {
        exitFunction = func
    }

    fun transition(block: TransitionBuilder.() -> Unit) {
        transitionsVar += TransitionBuilder().apply(block).build()
    }

    fun whenTrue(condition: () -> Boolean): TransitionToBuilder {
        return TransitionToBuilder(this, condition)
    }

    fun build(): State {

        if(!::stateName.isInitialized) {
            throw IllegalStateException("Called function must be invoked")
        }

        return object : State {
            override var transitions: List<Transition> = transitionsVar
            override var name: String = stateName()

            override fun onStart() = startFunction()
            override fun onLoop() = loopFunction()
            override fun onExit() = exitFunction()
        }
    }


    class TransitionToBuilder(val stateBuilder: StateBuilder, val condition: () -> Boolean) {
        infix fun transitionTo(state: () -> String) {
            this.stateBuilder.transitionsVar += object : Transition {
                override fun validate() = condition()
                override fun state() = state()
            }
        }
    }
}





class StateMachine(initialState: String, private val states: Map<String, State>) {
    init {
        val state = states[initialState]
        if (state != null) {
            transitionTo(state)
        }
    }

    var currentState: State? = null


    fun loop() {
        if(currentState == null) return

        for(transition in currentState?.transitions!!) {
            if(transition.validate()) {
                val s = states[transition.state()] ?: return
                transitionTo(s)
                return;
            }
        }

        currentState?.onLoop()
    }

    private fun transitionTo(state: State) {
        currentState?.onExit()
        currentState = state
        currentState?.onStart()
    }
}

class StateMachineBuilder {
    val states = mutableMapOf<String, State>()
    private lateinit var initialState: String

    fun initialState(func: () -> String) {
        initialState =  func()
    }

    fun inlineState(block: StateBuilder.() -> Unit) {
        val s = StateBuilder().apply(block).build()
        states[s.name] =  s
    }

    inline fun <reified T: BaseState> state(bloke: T.()-> Unit) {
        val s = T::class.java.getDeclaredConstructor().newInstance().apply(bloke)

        if(!s.isNameInitialized()) {
            throw IllegalStateException("Called function must be invoked")
        }

        states[s.name] = s
    }

    fun build(): StateMachine = StateMachine(initialState, states)
}

fun StateMachine(block: StateMachineBuilder.() -> Unit): StateMachine =
    StateMachineBuilder().apply(block).build()

interface Transition {
    fun validate(): Boolean
    fun state(): String
}


class TransitionBuilder {
    private lateinit var validateFunction: () -> Boolean
    private lateinit var stateFunction: () -> String

    fun condition(func: () -> Boolean) {
        validateFunction = func
    }

    fun state (func: () -> String) {
        stateFunction = func
    }

    fun build(): Transition = object : Transition {
        override fun validate() = validateFunction()
        override fun state() = stateFunction()
    }
}

fun transition(block: TransitionBuilder.() -> Unit): Transition = TransitionBuilder().apply(block).build()