package com.matteblack.fluent

abstract class BaseState: State {
    override lateinit var name: String
    override var transitions: List<Transition> = mutableListOf()

    fun whenTrue(condition: () -> Boolean): TransitionToBuilder {
        return TransitionToBuilder(this, condition)
    }

    fun called(name: () -> String) {
        this.name = name()
    }

    fun isNameInitialized() : Boolean {
        return ::name.isInitialized
    }
}


class TransitionToBuilder(private val base: BaseState, var validateFunction: () -> Boolean) {
    infix fun transitionTo(state: () -> String) {
        base.transitions += object : Transition {
            override fun validate() = validateFunction()
            override fun state() = state()
        }
    }
}
