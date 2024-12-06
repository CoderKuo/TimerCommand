package com.dakuo.timercommand

import org.bukkit.OfflinePlayer
import taboolib.common5.cdouble
import taboolib.common5.cint
import taboolib.platform.compat.PlaceholderExpansion

object PlaceholderHook : PlaceholderExpansion{
    override val identifier: String = "timercommand"

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        val params = args.splitByUnderscoreWithEscape().toMutableList()
        when (params.removeFirstOrNull()) {
            "process" -> { // %farm_process_10_1% %farm_process_10_1_10_&a|_&7|_<formatStr>%
                val max = params.removeFirstOrNull().cdouble
                val now = params.removeFirstOrNull().cdouble
                if (params.isEmpty()) {
                    return ProcessBar().getProgressText(now, max)
                } else {
                    val total = params.removeFirst().cint
                    val point = params.removeFirstOrNull()
                    val blank = params.removeFirstOrNull()
                    val formatStr = params.removeFirstOrNull()
                    return ProcessBar().apply {
                        setParams(total, point, blank, formatStr)
                    }.getProgressText(now, max)
                }
            }
        }
        return ""
    }

    fun String.splitByUnderscoreWithEscape(): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var escape = false

        for (i in this.indices) {
            val char = this[i]
            if (char == '\\' && !escape) {
                // 如果当前字符是 '\' 并且没有处于转义状态，进入转义状态
                escape = true
            } else if (char == '_' && !escape) {
                // 如果当前字符是下划线且没有转义，进行分割操作
                result.add(sb.toString())
                sb.clear()
            } else {
                // 将字符添加到当前段落
                if (escape && char == '_') {
                    // 转义的下划线变成普通下划线
                    sb.append('_')
                } else {
                    sb.append(char)
                }
                escape = false
            }
        }

        // 将最后一个段落添加到结果中
        if (sb.isNotEmpty()) {
            result.add(sb.toString())
        }

        return result
    }
}