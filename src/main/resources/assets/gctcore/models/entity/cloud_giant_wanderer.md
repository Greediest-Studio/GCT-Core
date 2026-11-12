# 流浪云巨人资源

已从用户在 Blockbench 中完成微调的最终工程保存并导出。可编辑工程是模型、UV、材质和动画的来源；后续调整应从该工程继续。

| 资源 | 路径（相对 assets/gctcore） |
| --- | --- |
| Blockbench 工程 | `models/entity/cloud_giant_wanderer.bbmodel` |
| GeckoLib 几何 | `geo/cloud_giant_wanderer.geo.json` |
| GeckoLib 动画 | `animations/cloud_giant_wanderer.animation.json` |
| 材质 | `textures/entity/cloud_giant_wanderer.png` |

几何标识为 `geometry.cloud_giant_wanderer`，共 41 个箱体、28 根骨骼；材质为 256×256，使用箱型 UV。前襟分片，后摆和侧摆具有独立避让曲线。

动画名称前缀为 `animation.cloud_giant_wanderer.`：

| 后缀 | 时长（秒） | 播放方式 |
| --- | ---: | --- |
| `idle` | 4.0 | 循环 |
| `walk` | 1.6 | 循环，位移交给实体 AI |
| `intro` | 3.0 | 单次 |
| `slam` | 1.6 | 单次 |
| `sweep` | 1.7 | 单次 |
| `storm_cast` | 3.2 | 单次 |
| `hurt` | 0.55 | 单次 |
| `phase_transition` | 3.0 | 单次 |
| `jump` | 0.65 | 单次；模型不做上下位移 |
| `forward_thrust` | 0.7 | 单次 |
| `whirlwind_cast` | 2.0 | 单次；双臂举杖 |
| `death` | 3.2 | 保持末帧 |

本项目 GeckoLib 版本不会从动画 JSON 的 `hold_on_last_frame` 字符串读取保持设置，而且其 `HOLD_ON_LAST_FRAME` 枚举尚未实现实际保持逻辑。游戏动画中 `death.loop` 为 `false`；实体控制器通过将死亡动画时间限制在末帧前的相邻浮点数来保持最终姿势。整身攻击、受伤和死亡应与行走状态切换，避免同时混合互相冲突的骨骼动作。

武器骨骼名为 `weapon`。后续 BOSS 制作可基于上述动画接入伤害判定、粒子、音效和阶段逻辑，并为实体单独设置碰撞箱；几何文件中的可见边界用于渲染。

## 云巨人祭司实体

实体 ID 为 `gctcore:cloud_giant_priest`，可使用刷怪蛋或 `/summon gctcore:cloud_giant_priest` 生成。主体渲染比例为 `64/114`，高度为 4 格；法杖略高于头顶，碰撞箱为 1×4×1。

服务端战斗逻辑位于 `EntityCloudGiantPriest`。随机名字保存在 `BossName` NBT 中，显示格式为 `<名字>, 云巨人祭司`。生命值为 500，移动速度为 0.3；单次生命损失不超过当前生命上限的 4%，拒绝没有实体来源的伤害。

| 技能 | 动画 / 时长 | 判定 |
| --- | --- | --- |
| 近战横扫 | `sweep` / 34 tick | 动画中点（第 17 tick）检查 5 格内的目标；满足距离和视线条件时造成 12–18 点伤害；动画结束后可再次使用 |
| 禁空锤击 | `slam` / 32 tick | 仇恨目标在空中或距离小于 7 格时使用；冷却 120 tick，开始后 0.8 秒（第 16 tick）对仍在 7 格球形范围内的目标造成 22 点伤害并强力击退玩家 |
| 唤雷 | `storm_cast` / 64 tick | 目标距离至少 10 格时使用；冷却 100 tick，施法结束造成 15 点 BOSS 直接伤害，雷电仅展示 |
| 云跃 / 前刺 | `jump` / 13 tick，接 `forward_thrust` / 14 tick | 二阶段目标距离大于 16 格时有 15% 概率发动；跃起阶段推动实体移动，前刺沿途命中一次造成 25 点伤害，云跃冷却 200 tick |
| 呼风 | `whirlwind_cast` / 40 tick | 二阶段目标距离大于 7 且不超过 16 格时使用；冷却 320 tick，动画结束后召唤 3 个 `aether_legacy:whirlwind`。被召唤旋风吸住的玩家每秒恢复最大生命值的 0.5% |
| 死亡 | `death` / 64 tick | 保持末帧 60 tick 后消失，取消原版侧翻与持续泛红 |

横扫可在动画中点之前通过离开判定范围或视线规避；砸地可在开始后 0.8 秒之前离开判定范围规避。两个动作都会继续播放到动画结束。BOSS 不会对创造模式玩家造成伤害，但仍可将其选为仇恨目标并播放攻击动作。禁空锤击仍会解除创造飞行玩家的当前飞行状态、击退玩家并将 `motionY` 设为 `-1.0`；对当前仇恨中的创造飞行玩家，这一压制不受距离限制。其他伤害为护甲和抗性结算前的数值，不随游戏难度倍增。

血条使用 `BossInfoServer` 同步。安装 Aether 时，客户端直接引用其 `aether_legacy:textures/gui/boss_bar.png` 并按 Aether 布局绘制；未安装时显示原版血条。动画依赖 GeckoLib 3。此次只接入战斗所需动画，保留原工程中的开场、受伤和阶段转换动画供后续扩展。

生命值首次降至 250 或更低时只触发一次 `phase_transition` 并解锁二阶段技能；转阶段动画播放期间完全免疫伤害。受伤动画仅在没有其他动作时播放，不会打断攻击、施法或移动技能。Aether 旋风的影响实体查询会排除云巨人祭司，避免它被旋风推动或参与旋风合并。
