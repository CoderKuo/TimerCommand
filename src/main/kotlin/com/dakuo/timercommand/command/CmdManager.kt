package com.dakuo.timercommand.command

import com.dakuo.timercommand.ProcessBar
import com.dakuo.timercommand.TimerCommand
import com.dakuo.timercommand.cmd.Reload
import com.dakuo.timercommand.cmd.ReloadFunction
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.cdouble
import taboolib.expansion.DurationType
import taboolib.expansion.fakeOp
import taboolib.expansion.submitChain
import taboolib.library.xseries.XSound
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import taboolib.module.configuration.util.getStringListColored
import taboolib.module.configuration.util.mapSection
import taboolib.module.chat.colored
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendActionBar
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

@Reload
object CmdManager {

    @Config("commands.yml")
    lateinit var config: Configuration

    private val cache = ConcurrentHashMap<String, CommandGroup>()

    private val processBar by lazy {
        val section = (TimerCommand.config.get("processBar") as? ConfigSection)
        val point = section?.getStringColored("point")
        val blank = section?.getStringColored("blank")
        val total = section?.getInt("total", 20)
        val format = section?.getStringColored("format")
        ProcessBar().apply { setParams(total, point, blank, format) }
    }
    @Awake(LifeCycle.ACTIVE)
    @ReloadFunction
    fun reload() {
        // 重新加载配置文件
        config.reload()
        // 清空缓存
        cache.clear()
        // 遍历配置文件中的每个部分
        config
            .mapSection {
                // 加载ActionBar配置
                val actionBar = (it.get("actionbar") as? ConfigSection)?.let {
                    val enable = it.getBoolean("enable", TimerCommand.getSetting().action.enable)
                    val text = it.getString("text", TimerCommand.getSetting().action.text)!!
                    ActionBar(enable, text)
                }

                // 加载BossBar配置
                val bossBar = (it.get("bossbar") as? ConfigSection)?.let {
                    val enable = it.getBoolean("enable", TimerCommand.getSetting().bossBar.enable)
                    val color = it.getString("color", TimerCommand.getSetting().bossBar.color)
                    val text = it.getString("text", TimerCommand.getSetting().bossBar.text)
                    val style = it.getString("style", TimerCommand.getSetting().bossBar.style)
                    BossBar(enable, color, text, style)
                }

                // 加载音效配置
                val sound = (it.get("sound") as? ConfigSection)?.let {
                    val enable = it.getBoolean("enable", TimerCommand.getSetting().sound.enable)
                    val tick = it.getString("tick", TimerCommand.getSetting().sound.tick)
                    val end = it.getString("end", TimerCommand.getSetting().sound.end)
                    Sound(enable, tick, end)
                }

                // 输出加载信息
                info(it.name + " 已加载")

                // 创建命令组对象
                CommandGroup(
                    it.getInt("time", 0),
                    it.getList("commands.console")?.map { 
                        if(it is Map<*,*>) {
                            ICommand.getCommand(it as Map<String, Any>)
                        } else {
                            ICommand.getCommand(it.toString())
                        }
                     } ?: emptyList(),
                    it.getList("commands.op")?.map { 
                        if(it is Map<*,*>) {
                            ICommand.getCommand(it as Map<String, Any>)
                        } else {
                            ICommand.getCommand(it.toString())
                        }
                     } ?: emptyList(),
                    it.getBoolean("allowMove", true), 
                    it.getList("commands.player")?.map { 
                        if(it is Map<*,*>) {
                            ICommand.getCommand(it as Map<String, Any>)
                        } else {
                            ICommand.getCommand(it.toString())
                        }
                     } ?: emptyList(),
                    actionBar,
                    bossBar,
                    sound
                )

            }
            // 将所有配置添加到缓存中
            .also { cache.putAll(it) }
    }

    fun getCmdIds() = cache.keys.toList()

    fun exec(key: String, player: Player, args: String? = null) {
        // 解析参数
        val parsedArgs = args?.split(" ")
        // 使用let替代also,更符合语义
        cache[key]?.let { command ->

            val sound = if (TimerCommand.getSetting().sound.enable || command.sound?.enable == true) {
                command.sound ?: TimerCommand.getSetting().sound
            } else {
                null
            }

            val actionBar = if (TimerCommand.getSetting().action.enable || command.action?.enable == true) {
                command.action ?: TimerCommand.getSetting().action
            } else {
                null
            }

            val bossBar = if (TimerCommand.getSetting().bossBar.enable || command.bossBar?.enable == true) {
                command.bossBar ?: TimerCommand.getSetting().bossBar
            } else {
                null
            }

            val tick = sound?.tick?.let {
                XSound.matchXSound(it).getOrNull()
            }

            val end = sound?.end?.let {
                XSound.matchXSound(it).getOrNull()
            }

            val time = command.time
            if (time > 0) {

                // 抽取BossBar创建逻辑
                val bar = bossBar?.let { bar ->
                    if (bar.text == null) return@let null

                    val style = BarStyle.valueOf(bar.style?.uppercase() ?: "SOLID")
                    val color = BarColor.valueOf(bar.color?.uppercase() ?: "GREEN")
                    Bukkit.createBossBar(
                        bar.text
                            .replace("%time%", time.toString())
                            .replaceArgs(parsedArgs),
                        color,
                        style
                    )
                        .apply {
                            progress = 0.0
                            addPlayer(player)
                        }
                }

                val startLocation = player.location

                submitChain {
                    for (i in 0..time) {
                        wait(1000, DurationType.MILLIS)

                        // 玩家下线直接结束
                        if (!player.isOnline) {
                            bar?.removePlayer(player)
                            return@submitChain
                        }

                        if (!command.allowMove) {
                            val nowLocation = player.location
                            if (startLocation.blockX != nowLocation.blockX ||
                                startLocation.blockY != nowLocation.blockY ||
                                startLocation.blockZ != nowLocation.blockZ
                            ) {
                                bar?.removePlayer(player)
                                return@submitChain
                            }
                        }

                        tick?.play(player)

                        // 更新ActionBar
                        actionBar?.let {
                            player.sendActionBar(
                                it.text
                                    .replaceArgs(parsedArgs)
                                    .replace(
                                        "%process%",
                                        processBar.getProgressText(
                                            i.cdouble,
                                            time.cdouble
                                        )
                                    )
                            )
                        }

                        // 更新BossBar
                        bar?.apply {
                            progress = i.cdouble / time.cdouble
                            setTitle(
                                bossBar?.text
                                    ?.replace("%time%", (time - i).toString())
                                    ?.replaceArgs(parsedArgs)
                            )
                        }
                    }

                    // 移除BossBar
                    bar?.removePlayer(player)

                    end?.play(player)

                    // 执行命令
                    val playerName = player.name
                    command.console.forEach { cmd ->
                        val finalCmd =
                            cmd.getCommand().replace("%player%", playerName)
                                .replacePlaceholder(player)
                                .replaceArgs(parsedArgs).colored()

                        if (finalCmd.isNotEmpty()) {
                            submit {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd)
                            }
                        }
                    }
                    command.op.forEach { cmd ->
                        cmd.getCommand().let {
                            if (it.isNotEmpty()) {
                                submit {
                                    player.fakeOp()
                                        .performCommand(
                                            it.replace("%player%", playerName)
                                                .replacePlaceholder(player)
                                                .replaceArgs(parsedArgs).colored()
                                        )
                                }
                            }
                        }

                    }
                    command.player.forEach { cmd ->
                        cmd.getCommand().let {
                            if (it.isNotEmpty()) {
                                submit {
                                    player.performCommand(
                                        it.replace("%player%", playerName)
                                            .replacePlaceholder(player)
                                            .replaceArgs(parsedArgs).colored()
                                    )

                                }
                            }
                        }
                    }
                }
            } else {
                // 执行命令
                val playerName = player.name
                    command.console.forEach { cmd ->
                        val finalCmd =
                            cmd.getCommand().replace("%player%", playerName)
                                .replacePlaceholder(player)
                                .replaceArgs(parsedArgs).colored()

                        if (finalCmd.isNotEmpty()) {
                            submit {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd)
                            }
                        }
                    }
                    command.op.forEach { cmd ->
                        cmd.getCommand().let {
                            if (it.isNotEmpty()) {
                                submit {
                                    player.fakeOp()
                                        .performCommand(
                                            it.replace("%player%", playerName)
                                                .replacePlaceholder(player)
                                                .replaceArgs(parsedArgs).colored()
                                        )
                                }
                            }
                        }

                    }
                    command.player.forEach { cmd ->
                        cmd.getCommand().let {
                            if (it.isNotEmpty()) {
                                submit {
                                    player.performCommand(
                                        it.replace("%player%", playerName)
                                            .replacePlaceholder(player)
                                            .replaceArgs(parsedArgs).colored()
                                    )

                                }
                            }
                        }
                    }
            }

        }
    }


    // 扩展函数用于替换参数
    private fun String.replaceArgs(args: List<String>?): String {
        if (args == null) return this
        var result = this
        args.forEachIndexed { index, arg -> result = result.replace("%arg_$index%", arg) }
        return result
    }
}
