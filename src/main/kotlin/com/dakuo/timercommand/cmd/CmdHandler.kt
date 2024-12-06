package com.dakuo.timercommand.cmd

import com.dakuo.timercommand.command.CmdManager
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.createHelper
import kotlin.run

@CommandHeader(name = "TimerCommand", aliases = ["tc"])
object CmdHandler {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permission = "timercommand.admin", permissionDefault = PermissionDefault.OP)
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, context, argument ->
            submitAsync {
                ReloadExecutor.execute()
                sender.sendMessage("重载成功")
            }
        }
    }

    @CommandBody
    val exec = subCommand {
        dynamic("id") {
            suggest {
                CmdManager.getCmdIds()
            }
            execute<ProxyCommandSender>{ sender, context, argument ->
                CmdManager.exec(argument,sender.cast())
            }
            dynamic("args") {
                execute<ProxyCommandSender>{ sender, context, argument ->
                    CmdManager.exec(context["id"],sender.cast(),argument)
                }
            }
        }
    }

    @CommandBody
    val execOther = subCommand {
        dynamic("id") {
            suggest {
                CmdManager.getCmdIds()
            }
            player{
                execute<ProxyCommandSender>{ sender,context,argument ->
                    val target = getProxyPlayer(argument) ?: run{
                        sender.sendMessage("§c玩家不存在")
                        return@execute
                    }
                    CmdManager.exec(context["id"],target.cast())
                }

                dynamic("args") {
                    execute<ProxyCommandSender>{ sender,context,argument ->
                        val target = getProxyPlayer(context["player"]) ?: run{
                            sender.sendMessage("§c玩家不存在")
                            return@execute
                        }
                        CmdManager.exec(context["id"],target.cast(),argument)
                    }
                }
            }
        }
    }
}