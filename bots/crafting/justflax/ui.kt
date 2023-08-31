package com.matteblack.bots.crafting.justflax

import com.matteblack.di.injected
import javafx.event.ActionEvent

class ui {

    val bot: JustFlax by injected()

    fun onStartPressed(actionEvent: ActionEvent) {
        bot.botStarted = true
    }
}