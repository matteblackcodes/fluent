package com.matteblack.utility

import com.runemate.game.api.hybrid.region.Players

object Delay {
    val MOVING = { Players.getLocal()?.isMoving == true }
    val MOVING_OR_ANIMATING = { Players.getLocal()?.isMoving == true || Players.getLocal()?.animationId != -1 }
}