package com.dakuo.timercommand.command


data class CommandGroup(
    val time: Int,
    val console: List<ICommand>,
    val op: List<ICommand>,
    val allowMove: Boolean,
    val player: List<ICommand>,
    val action: ActionBar?,
    val bossBar: BossBar?,
    val sound: Sound?
)

data class ActionBar(val enable:Boolean,val text:String)

data class BossBar(val enable: Boolean,val color:String?,val text:String?,val style:String?)

data class Sound(val enable:Boolean,val tick:String?,val end:String?)