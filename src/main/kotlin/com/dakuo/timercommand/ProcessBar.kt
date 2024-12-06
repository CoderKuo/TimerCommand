package com.dakuo.timercommand

import taboolib.module.chat.colored

class ProcessBar(
    private var totalBars: Int = 10, // 进度条的总长度
    private var pointChar: String = "■",
    private var blankChar: String = "□",
    private var formatStr: String = "%point%%blank% [%rate%%]"
) {

    fun setParams(
        totalBars: Int? = null,
        pointChar: String? = null,
        blankChar: String? = null,
        formatStr: String? = null
    ) {
        if (totalBars != null && totalBars > 0) {
            this.totalBars = totalBars
        }
        if (!pointChar.isNullOrEmpty()) {
            this.pointChar = pointChar.colored()
        }
        if (!blankChar.isNullOrEmpty()) {
            this.blankChar = blankChar.colored()
        }
        if (!formatStr.isNullOrEmpty()) {
            this.formatStr = formatStr.colored()
        }
    }

    fun getProgressText(current: Double, max: Double): String {
        require(current >= 0) { "Current progress 'current' must be non-negative." }
        if (max <= 0.0){
            return formatStr.format(blankChar.repeat(totalBars),"")
        }

        val progress = (current / max) * totalBars // 计算需要显示的进度
        val cappedProgress = progress.coerceAtMost(totalBars.toDouble()) // 限制进度不超过100%

        val filledBars = pointChar.repeat(cappedProgress.toInt()) // "■"的数量
        val emptyBars = blankChar.repeat(totalBars - cappedProgress.toInt()) // "□"的数量

        return formatStr.replace("%point%", filledBars).replace("%blank%", emptyBars).replace("%rate%", ((current / max) * 100).toInt().toString())
    }

}