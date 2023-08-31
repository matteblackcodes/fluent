package com.matteblack.bots.crafting.justflax

import com.matteblack.bots.crafting.justflax.states.SomeState
import com.matteblack.di.DIContainer
import com.matteblack.fluent.StateMachine
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.region.Banks
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.util.Resources
import com.runemate.game.api.hybrid.util.calculations.Distance
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.osrs.local.hud.interfaces.MakeAllInterface
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.LoopingBot
import com.runemate.ui.DefaultUI
import javafx.fxml.FXMLLoader
import javafx.scene.Node

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.scene.text.Font


const val START = "start"
const val MOVE_TO_SPIN = "moveToSpin"
const val SPIN = "spin"
const val MOVE_TO_BANK = "moveToBank"
const val BANK = "bank"
const val MOVE_TO_FLAX = "moveToFlax"
const val PICK_FLAX = "pickFlax"


class JustFlax: LoopingBot() {

    var botStarted = false;

    var player = Players.getLocal()
        get() {
            if(field == null) {
                field = Players.getLocal()
            }

            return field!!;
        }


    override fun onStart(vararg arguments: String?) {
        DIContainer.register(this)

        DefaultUI.addPanel(0, this, "JustFlax", loadFXML(), true)

        super.onStart(*arguments)
    }


    val stateMachine = StateMachine {

        initialState { START }


        inlineState {
            called { START }

            transition {
                condition { Inventory.isFull() && Inventory.containsOnly("Flax") }
                state { MOVE_TO_SPIN }
            }

            transition {
                condition {
                    !Inventory.containsOnly("Bow string", "Flax") || Inventory.isFull()
                }
                state { MOVE_TO_BANK }
            }

            transition {
                condition { true }
                state { MOVE_TO_FLAX }
            }

        }

        inlineState {
            called  { MOVE_TO_SPIN }

            val spinCoordinate = Coordinate(2711, 3471, 1)

            transition {
                //Not sure if this will work...
                condition { spinCoordinate.isReachable }
                state { SPIN }
            }

            loop {
                WebPath.buildTo(spinCoordinate).step()
            }
        }


        inlineState {
            called  { SPIN }

            whenTrue { !Inventory.contains("Flax") } transitionTo { MOVE_TO_BANK }

            loop {
                if (isSpinning()) return@loop

                GameObjects.newQuery()
                    .names("Spinning wheel")
                    .actions("Spin")
                    .results()
                    .nearest()?.interact("Spin")


                Execution.delayUntil({MakeAllInterface.isOpen()}, 2000)
                if(MakeAllInterface.isOpen()) {
                    MakeAllInterface.setSelectedQuantity(0)
                    MakeAllInterface.selectItem("Bow String")
                }


            }


        }

        inlineState {
            called  { MOVE_TO_BANK }

            val bankCoordinate = Coordinate(2725, 3491, 0)

            var booth = Banks.getLoadedBankBooths().nearest()


            whenTrue { Distance.between(player, booth) <= 10 } transitionTo { BANK }

            loop {
                WebPath.buildTo(bankCoordinate).step()
                booth = Banks.getLoadedBankBooths().nearest()
            }
        }


        inlineState {
            called  { BANK }

            transition {
                condition { Inventory.isEmpty() && !Bank.isOpen() }
                state { MOVE_TO_FLAX }
            }

            loop {
                if(!Bank.open()) return@loop

                Bank.depositInventory()
                Bank.close()
            }
        }


        inlineState {
            called  { MOVE_TO_FLAX }

            val flaxCoordinate = Coordinate(2736, 3443, 0)

            transition {
                condition {
                    Distance.between(player, flaxCoordinate) <= 5
                }
                state { PICK_FLAX }
            }

            loop {

                WebPath.buildTo(flaxCoordinate).step()
            }
        }


        inlineState {
            called  { PICK_FLAX }

            transition {
                condition { Inventory.isFull() }
                state { MOVE_TO_SPIN }
            }

            loop {
                GameObjects.newQuery()
                    .names("Flax")
                    .actions("Pick")
                    .results()
                    .nearest()?.interact("Pick")

                Execution.delay(200, 300)
            }
        }
    }

    override fun onLoop() {
        if(!botStarted) return
        stateMachine.loop()
    }

    fun isSpinning(): Boolean {
        Execution.delayUntil({ player?.animationId != -1 }, { player?.isMoving }, 2000 )
        return player?.animationId != -1
    }

    fun loadFXML(): Node {
        val loader = FXMLLoader()
        val inputStream = Resources.getAsStream("com/matteblack/bots/crafting/justflax/ui.fxml")
        return loader.load(inputStream)
    }
}

