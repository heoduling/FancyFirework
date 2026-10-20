# FancyFirework（26.2 Folia 汉化版）

本分支适配所提供的 Shiroha 26.2 Folia 核心，并提供中文物品名称、默认说明、命令反馈和管理员烟花大全 GUI。插件内置 29 种烟花：13 种烟花筒、11 种喷泉烟花和 5 种可投掷烟花。

## 管理员烟花大全

拥有 `fancyfirework.admin` 权限的玩家输入 `/ff menu`（或 `/fancyfirework menu`）打开界面。界面从实际烟花注册表读取全部内置类型；左键领取 1 个，中键或 Shift+左键领取一组（一组数量以该物品的最大堆叠数为准）。背包空间不足时不会发放。其他点击、拖拽和下方背包操作在界面开启期间均被取消。

26.2 API 将中键点击列为创造模式动作；生存模式请使用 Shift+左键领取一组。GUI 打开期间，PlugManX 的 `reload` 和 `unload` 会拒绝操作；关闭所有管理员烟花界面后重试即可。直接通过其他插件管理器禁用插件时，需先关闭这些界面。

## 获得烟花

- 可通过 NuVotifier 投票奖励及箱子战利品表获得烟花。
- 拥有 `fancyfirework.mod` 权限的玩家可使用 `/ff give <烟花键> <数量> [玩家名]` 领取烟花；发给他人还需 `fancyfirework.admin` 权限。烟花键可通过 Tab 补全。

## 主要命令与权限

| 命令 | 权限 | 用途 |
| --- | --- | --- |
| `/ff help` | 无 | 按权限显示可执行命令；也可输入 `/ff` 查看帮助入口 |
| `/ff menu` | `fancyfirework.admin` | 打开烟花大全 |
| `/ff use enable`、`/ff use disable` | `fancyfirework.admin` | 开关烟花使用 |
| `/ff give <烟花键> <数量> [玩家名]` | `fancyfirework.mod`，发给他人还需 `fancyfirework.admin` | 发放烟花 |
| `/ff reload` | `fancyfirework.admin` | 重新读取配置 |
| `/ff shopgive <玩家名> <烟花键> <数量>` | 仅服务器控制台 | 给在线玩家发放商店烟花，数量 1–2304；背包空间不足时不发放 |

## UltimateShop 命令商品

UltimateShop 可用 `display-item` 展示对应头颅贴图或投掷物模型，并在 `buy-actions` 中执行 `ff shopgive %player_name% fancyfirework:battery_blue {amount}`。启用多数量购买时，该动作设为 `multi-once: true`，并确保展示物品的 `calculate-amount: true`、数量为 1。菜单展示物品不需要携带烟花标记；实际发给玩家的物品由 FancyFirework 生成。此命令仅接受在线玩家，背包空间不足、玩家离线或插件停用时会拒绝发放并写入日志；UltimateShop 的命令商品不会因此自动退款，需由管理员处理该笔交易。不设置出售价格时，该商品只能购买。

## 配置

默认配置位于 `src/main/resources/config.yml`。旧配置中的原版英文默认物品说明会在首次启用时自动改为中文；服主自行修改过的说明保持原样。已经发给玩家的旧物品不会被自动改名。旧配置中的 `enabled`、`spawnrate` 和 `maxAFKTime` 已不再使用。

```yaml
votereward: false    # 是否启用投票奖励
redstonemode: false  # 是否允许红石点燃
litplayer: true      # 是否允许在玩家头上点燃烟花
```

## 构建与安装

使用 Java 25 执行 `mvn -Pshade clean verify`，独立插件产物为 `target/fancyfirework-shade.jar`。将其放入 26.2 Folia 服务端的 `plugins` 目录。已在所提供的 Shiroha 26.2 隔离服和 PlugManX 3.1.0-Beta.2 上验证插件启动与热加载。NuVotifier、TreasureChest、WorldGuard 和 ChestShop 等可选联动仍需对应插件的环境验证。

原项目：[fantahund/FancyFirework](https://github.com/fantahund/FancyFirework)。
