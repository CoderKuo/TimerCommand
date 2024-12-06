package com.dakuo.timercommand.command

import org.bukkit.entity.Player
import kotlin.random.Random

interface ICommand {
    fun getCommand():String

    companion object {
        fun getCommand(section: Map<String, Any>): ICommand {
            return when(section["type"] as? String) {
                "random" -> {
                    val commands = section["commands"] as? List<String> ?: emptyList()
                    RandomCommand(commands.map { SimpleCommand(it) })
                }
                "chance" -> {
                    val chance = section["chance"] as? Double ?: 0.0
                    val command = section["command"]
                    if(command is Map<*,*>) {
                        ChanceCommand(getCommand(command as Map<String, Any>), chance)
                    } else {
                        ChanceCommand(SimpleCommand(command.toString()), chance)
                    }
                }
                "weight" -> {
                    val commands = section["commands"] as? List<Map<String, Any>> ?: emptyList()
                    val weightMap = mutableMapOf<Double, ICommand>()
                    commands.forEach { cmd ->
                        val weight = cmd["weight"].toString().toDoubleOrNull() ?: 0.0
                        val command = cmd["command"] as? String ?: ""
                        weightMap[weight] = SimpleCommand(command)
                    }
                    RandomWeightCommand(weightMap)
                }
                else -> SimpleCommand(section.toString())
            }
        }

        fun getCommand(cmd: String): ICommand {
            return SimpleCommand(cmd)
        }
    }
}

class SimpleCommand(val cmd: String): ICommand {
    override fun getCommand(): String {
        return cmd
    }
}

class RandomCommand(val commands: List<ICommand>): ICommand {
    override fun getCommand(): String {
        if(commands.isEmpty()) return ""
        val random = Random.nextInt(commands.size)
        return commands[random].getCommand()
    }
}

class ChanceCommand(val command: ICommand, val chance: Double): ICommand {
    override fun getCommand(): String {
        return if(Random.nextDouble() <= chance) {
            command.getCommand()
        } else {
            ""
        }
    }
}

class RandomWeightCommand(val weight: Map<Double, ICommand>): ICommand {
    override fun getCommand(): String {
        val totalWeight = weight.keys.sum()
        if(totalWeight == 0.0) return ""
        
        var currentWeight = 0.0
        val random = Random.nextDouble() * totalWeight
        
        for((w, cmd) in weight) {
            currentWeight += w
            if(currentWeight >= random) {
                return cmd.getCommand()
            }
        }
        
        return ""
    }
}
