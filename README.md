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
logo/                     图标预览与深色版图标资源
```

## 版本
- v1.0 单应用监听 → v1.1 多应用监听 → v1.2 图标与改名「码到」→ v1.3 去掉系统标题栏、自绘实心开关 → v1.4 开关改为森林绿 → v1.5 以 v1.2 外观为准（恢复原标题栏），仅开关为森林绿
