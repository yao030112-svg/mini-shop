# 微信小程序项目安装和编译脚本
# 使用方法：右键 -> 使用PowerShell运行

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  微信小程序项目安装和编译脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置工作目录
$projectPath = "D:\小程序开发练习\mini-shop-frontend"
Set-Location $projectPath

Write-Host "1. 检查Node.js版本..." -ForegroundColor Yellow
$nodeVersion = node -v
Write-Host "   Node.js版本: $nodeVersion" -ForegroundColor Green

Write-Host ""
Write-Host "2. 配置npm镜像源（使用淘宝镜像）..." -ForegroundColor Yellow
npm config set registry https://registry.npmmirror.com
Write-Host "   配置完成！" -ForegroundColor Green

Write-Host ""
Write-Host "3. 清理旧的依赖..." -ForegroundColor Yellow
if (Test-Path "node_modules") {
    Write-Host "   正在删除 node_modules..." -ForegroundColor Gray
    Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}
if (Test-Path "package-lock.json") {
    Write-Host "   正在删除 package-lock.json..." -ForegroundColor Gray
    Remove-Item package-lock.json -ErrorAction SilentlyContinue
}
Write-Host "   清理完成！" -ForegroundColor Green

Write-Host ""
Write-Host "4. 安装依赖（这可能需要几分钟）..." -ForegroundColor Yellow
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "   依赖安装失败！请检查：" -ForegroundColor Red
    Write-Host "   - 网络连接是否正常" -ForegroundColor Gray
    Write-Host "   - 是否有进程占用 node_modules 文件夹" -ForegroundColor Gray
    Write-Host "   - 尝试重启电脑后再运行此脚本" -ForegroundColor Gray
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "   依赖安装完成！" -ForegroundColor Green

Write-Host ""
Write-Host "5. 编译微信小程序..." -ForegroundColor Yellow
Write-Host "   提示：编译会在后台持续运行，按 Ctrl+C 停止" -ForegroundColor Gray
Write-Host ""
npm run dev:mp-weixin
