# TimerCommand

一个支持延迟执行、随机执行和概率执行的命令插件。

## 功能特点

- 支持设置命令延迟执行时间
- 支持控制台、OP权限和玩家身份执行命令
- 支持随机执行命令组中的一条命令
- 支持按概率执行命令
- 支持按权重随机执行命令
- 支持命令组合使用
- 可配置ActionBar、BossBar显示进度
- 可配置执行音效

## 构建发行版本

发行版本用于正常使用, 不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体, 用于开发者使用, 但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 -PDeleteCode 表示移除所有逻辑代码以减少体积。