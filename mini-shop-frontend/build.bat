@echo off
chcp 65001 >nul
echo 正在清理 node_modules...
rmdir /s /q node_modules 2>nul
del package-lock.json 2>nul
echo 正在安装依赖...
npm install --no-optional
if %errorlevel% neq 0 (
    echo 依赖安装失败，请检查网络连接
    pause
    exit /b 1
)
echo 依赖安装完成！
echo 正在编译微信小程序...
npm run dev:mp-weixin
pause
