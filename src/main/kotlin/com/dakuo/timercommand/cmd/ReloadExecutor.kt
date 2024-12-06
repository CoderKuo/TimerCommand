package com.dakuo.timercommand.cmd

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.library.reflex.ClassMethod
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ReflexClass
import java.util.function.Supplier
import java.lang.reflect.Method

annotation class Reload(vararg val methods: String = ["reload"])

annotation class ReloadFunction(val priority:Int = 10)

@Awake
object ReloadExecutor : ClassVisitor(-10) {

    // 改为存储方法、实例以及优先级
    private val reloadMethods = mutableSetOf<Triple<Method, Any, Int>>()

    override fun visit(field: ClassField, owner: ReflexClass) {
        if (owner.hasAnnotation(Reload::class.java)) {
            owner.toClass().methods.forEach { method ->
                if (method.isAnnotationPresent(ReloadFunction::class.java)) {
                    val priority = method.getAnnotation(ReloadFunction::class.java).priority
                    owner.getInstance()?.let { instance ->
                        reloadMethods.add(Triple(method, instance, priority))
                    }
                }
            }
        }
    }

    fun execute() {
        // 按优先级排序后执行
        reloadMethods
            .sortedBy { it.third } // 按优先级从小到大排序
            .forEach { it.first.invoke(it.second) }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ACTIVE
    }

}