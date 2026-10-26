# 码到 (MaDao)

自动获取各来源的验证码：读取通知里的验证码，复制到剪贴板，并自动填入当前输入框后回车。

## 工作方式
- 通知使用权读取指定应用的通知（预设：ntfy、Gmail、QQ 邮箱、Outlook、微信、Telegram，也可自定义包名）
- 通知里要有关键字（验证码 / 校验码 / OTP / code 等）加 4–8 位数字才会被识别
- 无障碍服务把验证码写入当前焦点输入框，并发送回车
- 事件驱动，不轮询；与 XposedSmsCode 相互独立，不需要 root

## 使用
1. 安装 APK，打开「码到」
2. 依次开启：通知使用权、无障碍服务、取消电池优化
3. 在「监听的应用」里打开要监听的来源

注意：通知必须显示正文（不能被设为隐藏内容）；灭屏时只复制，不会填入。

## 构建
需要 JDK 17 和 Android SDK（compileSdk 35）。

```
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=$HOME/Library/Android/sdk \
gradle :app:testDebugUnitTest :app:assembleDebug
```

仓库放在 iCloud 里时，建议复制到别处再构建。

## 目录
```
app/src/main/java/com/zbs/ntfybridge/
  CodeExtractor.kt        提取验证码（有单元测试）
  NtfyListenerService.kt  监听通知
  AutoFillService.kt      无障碍：填入并回车
  HomeActivity.kt         主界面（含深色模式）
  Prefs.kt                设置与来源列表
  ToggleView.kt           自绘开关（52×32dp，滑块始终在轨道内）
logo/                     图标预览与深色版图标资源
```

## 版本规则
- **显示版本号（versionName）只在有实际的新功能 / 新适配时才增加**，修小问题、调颜色、改文案都不增加
- 构建号（versionCode）每次出安装包都 +1，只是为了让新包能覆盖旧包，界面里不显示
- git 标签：`v1.x` 标记功能里程碑；`build-N` 标记第 N 号构建（N = versionCode）

## 版本
- v1.0 重新设计的界面，监听单个应用（ntfy）
- v1.1 同时监听多个应用（Gmail / QQ 邮箱 / Outlook / 微信 / Telegram，可自定义包名）
- v1.2 改名「码到」、新图标、深色模式、自绘开关、标题栏显示版本号
- 之后的小修补只增加构建号（build-4 … build-12），例如：开关颜色、通知使用权文案、v1.8 的混合验证码（已舍弃，留在分支 archive/v1.8）
