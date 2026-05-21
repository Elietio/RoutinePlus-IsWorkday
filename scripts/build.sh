#!/bin/bash
# 编译并构建 Debug 版本 APK (兼容 Windows 与 Bash 环境)
cmd.exe /c "gradlew.bat assembleDebug --build-cache --parallel"
