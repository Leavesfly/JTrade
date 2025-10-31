@echo off
REM ##############################################################################
REM JTrade 启动脚本 (Windows)
REM ##############################################################################

setlocal enabledelayedexpansion

REM 获取脚本所在目录
set "SCRIPT_DIR=%~dp0"
set "BASE_DIR=%SCRIPT_DIR%.."

REM JAR文件路径
set "JAR_FILE=%BASE_DIR%\lib\jtrade.jar"

REM 配置文件路径
set "CONFIG_DIR=%BASE_DIR%\config"

REM JVM参数
if "%JAVA_OPTS%"=="" (
    set "JAVA_OPTS=-Xms512m -Xmx2048m"
)

REM Spring配置路径
set "SPRING_CONFIG_LOCATION=file:%CONFIG_DIR%/"

REM 打印Banner
:print_banner
echo.
echo ================================================================================
echo    ___  _____              _      
echo   ^|_  ^|^|_   _^|            ^| ^|     
echo     ^| ^|  ^| ^|_ __ __ _  __^| ^| ___ 
echo     ^| ^|  ^| ^| '__/ _` ^|/ _` ^|/ _ \
echo /\__/ /  ^| ^| ^| ^| (^_^| ^| (^_^| ^|  __/
echo \____/   \_/_^|  \__,_^\__,_^|\_^_^_^|
echo.
echo   JTrade - 多智能体交易决策系统
echo   Version: 1.0.0
echo ================================================================================
echo.
goto :eof

REM 检查Java环境
:check_java
if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

where !JAVA_CMD! >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Java环境
    echo 请设置JAVA_HOME环境变量或安装JDK 17+
    exit /b 1
)

echo [√] Java环境检查通过
!JAVA_CMD! -version 2>&1 | findstr "version"
echo.
goto :eof

REM 检查JAR文件
:check_jar
if not exist "%JAR_FILE%" (
    echo [错误] 未找到JAR文件
    echo   路径: %JAR_FILE%
    echo 请先执行: mvn clean package
    exit /b 1
)
echo [√] JAR文件检查通过
echo   路径: %JAR_FILE%
echo.
goto :eof

REM 检查环境变量
:check_env
echo 检查环境变量...

if "%OPENAI_API_KEY%"=="" (
    echo [警告] 未设置OPENAI_API_KEY环境变量
    echo   某些功能可能无法使用
) else (
    echo [√] OPENAI_API_KEY已设置
)

if "%FINNHUB_API_KEY%"=="" (
    echo [警告] 未设置FINNHUB_API_KEY环境变量
    echo   将使用模拟数据
) else (
    echo [√] FINNHUB_API_KEY已设置
)
echo.
goto :eof

REM 显示帮助信息
:show_help
echo 用法: %~nx0 [模式] [选项]
echo.
echo 模式:
echo   demo          运行演示程序 (默认)
echo   cli           运行CLI命令行工具
echo   integration   运行完整集成演示
echo.
echo 选项:
echo   --help        显示此帮助信息
echo.
echo 示例:
echo   %~nx0                    # 运行默认演示
echo   %~nx0 demo               # 运行演示程序
echo   %~nx0 cli                # 运行CLI工具
echo   %~nx0 integration        # 运行集成演示
echo.
echo 环境变量:
echo   JAVA_OPTS          JVM参数 (默认: -Xms512m -Xmx2048m)
echo   OPENAI_API_KEY     OpenAI API密钥
echo   FINNHUB_API_KEY    Finnhub API密钥
echo.
goto :eof

REM 启动应用
:start_app
set "MAIN_CLASS=%~1"
shift

echo [启动] JTrade...
echo   主类: %MAIN_CLASS%
echo   JVM参数: %JAVA_OPTS%
echo.

"%JAVA_CMD%" %JAVA_OPTS% ^
    -Dspring.config.location="%SPRING_CONFIG_LOCATION%" ^
    -Dfile.encoding=UTF-8 ^
    -jar "%JAR_FILE%" ^
    --spring.main.class="%MAIN_CLASS%" ^
    %*

goto :eof

REM 主函数
:main
call :print_banner

REM 解析参数
set "MODE=%~1"
if "%MODE%"=="" set "MODE=demo"

if "%MODE%"=="--help" (
    call :show_help
    exit /b 0
)

REM 环境检查
call :check_java
if errorlevel 1 exit /b 1

call :check_jar
if errorlevel 1 exit /b 1

call :check_env

REM 根据模式启动
if "%MODE%"=="demo" (
    call :start_app "io.leavesfly.jtrade.JTradeDemoApplication"
) else if "%MODE%"=="cli" (
    call :start_app "io.leavesfly.jtrade.cli.JTradeCLI"
) else if "%MODE%"=="integration" (
    call :start_app "io.leavesfly.jtrade.demo.IntegrationDemo"
) else (
    echo [错误] 未知模式 '%MODE%'
    echo.
    call :show_help
    exit /b 1
)

goto :eof

REM 执行主函数
call :main %*
