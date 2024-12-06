package com.dakuo.timercommand

import com.dakuo.timercommand.cmd.Reload
import com.dakuo.timercommand.command.ActionBar
import com.dakuo.timercommand.command.BossBar
import com.dakuo.timercommand.command.Sound
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored

@Reload
object TimerCommand : Plugin() {

    @Config
    lateinit var config:Configuration

    private lateinit var setting:Setting


    override fun onEnable() {
        
        setting = Setting(config.get("default") as ConfigSection)
        info("Successfully running TimerCommand!")
    }

    fun getSetting(): Setting {
        return if (::setting.isInitialized){
            setting
        }else{
            setting = Setting(config.get("default") as ConfigSection)
            setting
        }
    }

    fun reload(){
        info("Reloading TimerCommand...")
        config.reload()
        setting = Setting(config.get("default") as ConfigSection)
    }

    class Setting(section: ConfigSection){

        val action = ActionBar(section.getBoolean("actionbar.enable",false),section.getStringColored("actionbar.text") ?: "")

        val bossBar = BossBar(section.getBoolean("bossBar.enable",false),section.getString("bossBar.color","green")!!,section.getStringColored("bossBar.text") ?: "",section.getString("bossBar.style") ?: "SOLID")

        val sound = Sound(section.getBoolean("sound.enable",false),section.getString("sound.tick"),section.getString("sound.end"))


    }
}