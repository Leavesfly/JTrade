#!/bin/bash
##############################################################################
# JTrade 启动脚本 (Unix/Linux/Mac)
##############################################################################

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# JAR文件路径
JAR_FILE="$BASE_DIR/lib/jtrade.jar"

# 配置文件路径
CONFIG_DIR="$BASE_DIR/config"

# JVM参数
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx2048m}"

# Spring配置路径
SPRING_CONFIG_LOCATION="file:$CONFIG_DIR/"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印Banner
print_banner() {
    echo ""
    echo "================================================================================"
    echo "   ___  _____              _      "
    echo "  |_  ||_   _|            | |     "
    echo "    | |  | |_ __ __ _  __| | ___ "
    echo "    | |  | | '__/ _\` |/ _\` |/ _ \\"
    echo "/\\__/ /  | | | | (_| | (_| |  __/"
    echo "\\____/   \\_/_|  \\__,_|\\__,_|\\___|"
    echo ""
    echo "  JTrade - 多智能体交易决策系统"
    echo "  Version: 1.0.0"
    echo "================================================================================"
    echo ""
}

# 检查Java环境
check_java() {
    if [ -n "$JAVA_HOME" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    else
        JAVA_CMD="java"
    fi
    
    if ! command -v "$JAVA_CMD" &> /dev/null; then
        echo -e "${RED}错误: 未找到Java环境${NC}"
        echo "请设置JAVA_HOME环境变量或安装JDK 17+"
        exit 1
    fi
    
    # 检查Java版本
    JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo -e "${RED}错误: Java版本过低，需要JDK 17+${NC}"
        echo "当前版本: $JAVA_VERSION"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Java环境检查通过${NC}"
    echo "  Java命令: $JAVA_CMD"
    "$JAVA_CMD" -version 2>&1 | head -n 1
    echo ""
}

# 检查JAR文件
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        echo -e "${RED}错误: 未找到JAR文件${NC}"
        echo "  路径: $JAR_FILE"
        echo "请先执行: mvn clean package"
        exit 1
    fi
    echo -e "${GREEN}✓ JAR文件检查通过${NC}"
    echo "  路径: $JAR_FILE"
    echo ""
}

# 检查环境变量
check_env() {
    echo "检查环境变量..."
    
    if [ -z "$OPENAI_API_KEY" ]; then
        echo -e "${YELLOW}警告: 未设置OPENAI_API_KEY环境变量${NC}"
        echo "  某些功能可能无法使用"
    else
        echo -e "${GREEN}✓ OPENAI_API_KEY已设置${NC}"
    fi
    
    if [ -z "$FINNHUB_API_KEY" ]; then
        echo -e "${YELLOW}警告: 未设置FINNHUB_API_KEY环境变量${NC}"
        echo "  将使用模拟数据"
    else
        echo -e "${GREEN}✓ FINNHUB_API_KEY已设置${NC}"
    fi
    echo ""
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [模式] [选项]"
    echo ""
    echo "模式:"
    echo "  demo          运行演示程序 (默认)"
    echo "  cli           运行CLI命令行工具"
    echo "  integration   运行完整集成演示"
    echo "  custom        自定义主类运行"
    echo ""
    echo "选项:"
    echo "  --help        显示此帮助信息"
    echo "  --main-class  指定主类 (仅用于custom模式)"
    echo ""
    echo "示例:"
    echo "  $0                                    # 运行默认演示"
    echo "  $0 demo                               # 运行演示程序"
    echo "  $0 cli                                # 运行CLI工具"
    echo "  $0 integration                        # 运行集成演示"
    echo "  $0 custom --main-class io.leavesfly.jtrade.demo.BatchTestDemo"
    echo ""
    echo "环境变量:"
    echo "  JAVA_OPTS          JVM参数 (默认: -Xms512m -Xmx2048m)"
    echo "  OPENAI_API_KEY     OpenAI API密钥"
    echo "  FINNHUB_API_KEY    Finnhub API密钥"
    echo ""
}

# 启动应用
start_app() {
    local MAIN_CLASS=$1
    
    echo -e "${GREEN}启动 JTrade...${NC}"
    echo "  主类: $MAIN_CLASS"
    echo "  JVM参数: $JAVA_OPTS"
    echo ""
    
    "$JAVA_CMD" $JAVA_OPTS \
        -Dspring.config.location="$SPRING_CONFIG_LOCATION" \
        -Dfile.encoding=UTF-8 \
        -jar "$JAR_FILE" \
        --spring.main.class="$MAIN_CLASS" \
        "$@"
}

# 主函数
main() {
    print_banner
    
    # 解析参数
    MODE="${1:-demo}"
    
    if [ "$MODE" = "--help" ]; then
        show_help
        exit 0
    fi
    
    # 环境检查
    check_java
    check_jar
    check_env
    
    # 根据模式启动
    case "$MODE" in
        demo)
            start_app "io.leavesfly.jtrade.JTradeDemoApplication" "${@:2}"
            ;;
        cli)
            start_app "io.leavesfly.jtrade.cli.JTradeCLI" "${@:2}"
            ;;
        integration)
            start_app "io.leavesfly.jtrade.demo.IntegrationDemo" "${@:2}"
            ;;
        custom)
            shift
            if [ "$1" = "--main-class" ]; then
                shift
                CUSTOM_CLASS="$1"
                shift
                start_app "$CUSTOM_CLASS" "$@"
            else
                echo -e "${RED}错误: custom模式需要指定--main-class参数${NC}"
                show_help
                exit 1
            fi
            ;;
        *)
            echo -e "${RED}错误: 未知模式 '$MODE'${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
