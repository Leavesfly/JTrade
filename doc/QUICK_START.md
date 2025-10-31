# JTrade 快速使用指南

## 📦 可执行JAR包使用

### 1. 构建项目

```bash
cd JTrade
mvn clean package -DskipTests
```

### 2. 运行方式

#### 方式一：直接运行JAR包

```bash
# 运行默认演示程序
java -jar target/jtrade-1.0.0-exec.jar

# 运行CLI命令行工具
java -jar target/jtrade-1.0.0-exec.jar --spring.main.class=io.leavesfly.jtrade.cli.JTradeCLI

# 运行集成演示
java -jar target/jtrade-1.0.0-exec.jar --spring.main.class=io.leavesfly.jtrade.demo.IntegrationDemo

# 运行交互式演示
java -jar target/jtrade-1.0.0-exec.jar --spring.main.class=io.leavesfly.jtrade.demo.InteractiveDemo
```

#### 方式二：使用启动脚本（推荐）

解压分发包后使用：

**Unix/Linux/Mac:**
```bash
# 解压分发包
tar -xzf target/jtrade-1.0.0-distribution.tar.gz
cd jtrade-1.0.0

# 运行演示程序
./bin/jtrade.sh demo

# 运行CLI工具
./bin/jtrade.sh cli

# 运行集成演示
./bin/jtrade.sh integration
```

**Windows:**
```cmd
REM 解压分发包
unzip target\jtrade-1.0.0-distribution.zip
cd jtrade-1.0.0

REM 运行演示程序
bin\jtrade.bat demo

REM 运行CLI工具
bin\jtrade.bat cli

REM 运行集成演示
bin\jtrade.bat integration
```

### 3. JVM参数调优

```bash
# 设置内存参数
export JAVA_OPTS="-Xms1g -Xmx4g"
./bin/jtrade.sh demo

# 或直接在java命令中指定
java -Xms1g -Xmx4g -jar target/jtrade-1.0.0-exec.jar
```

### 4. 环境变量配置

```bash
# 设置LLM API密钥
export OPENAI_API_KEY=your_openai_api_key
export FINNHUB_API_KEY=your_finnhub_api_key

# 运行
./bin/jtrade.sh demo
```

## 📁 分发包结构

```
jtrade-1.0.0/
├── bin/
│   ├── jtrade.sh      # Unix/Linux/Mac启动脚本
│   └── jtrade.bat     # Windows启动脚本
├── lib/
│   └── jtrade.jar     # 可执行JAR包
├── config/
│   ├── application.properties
│   └── prompts/
│       └── agent-prompts.properties
└── docs/
    ├── README.md
    └── PROJECT_STATUS.md
```

## 🚀 使用示例

### CLI模式

```bash
$ ./bin/jtrade.sh cli

jtrade> analyze AAPL 2024-05-10
# 分析AAPL股票

jtrade> history AAPL
# 查看AAPL历史决策

jtrade> list
# 列出所有决策记录

jtrade> help
# 查看帮助信息

jtrade> exit
# 退出
```

### 交互式模式

```bash
$ ./bin/jtrade.sh integration

# 选择演示模式
请选择演示模式：
1. 使用 TradingGraph（图编排模式）
2. 使用 TradingService（服务模式）
3. 对比两种模式

请输入选择 (1/2/3): 3

# 输入分析参数
请输入股票代码 (默认: AAPL): TSLA
请输入日期 (格式: YYYY-MM-DD, 默认: 今天): 2024-05-10

# 等待分析完成...
```

## 🔧 故障排查

### 问题1：Java版本不兼容

```bash
错误: 未找到Java环境或版本过低

解决方案:
1. 安装JDK 17或更高版本
2. 设置JAVA_HOME环境变量
   export JAVA_HOME=/path/to/jdk17
```

### 问题2：内存不足

```bash
错误: java.lang.OutOfMemoryError

解决方案:
export JAVA_OPTS="-Xms2g -Xmx4g"
./bin/jtrade.sh demo
```

### 问题3：API密钥未设置

```bash
警告: 未设置OPENAI_API_KEY环境变量

解决方案:
export OPENAI_API_KEY=your_key
export FINNHUB_API_KEY=your_key
```

## 📊 生成的文件

执行`mvn clean package`后，在`target/`目录下生成：

| 文件 | 大小 | 说明 |
|-----|------|------|
| `jtrade-1.0.0.jar` | ~188KB | 普通JAR（不含依赖） |
| `jtrade-1.0.0-exec.jar` | ~26MB | 可执行JAR（包含所有依赖） |
| `jtrade-1.0.0-distribution.tar.gz` | ~24MB | Unix/Linux/Mac分发包 |
| `jtrade-1.0.0-distribution.zip` | ~24MB | Windows分发包 |

## 🎯 推荐用法

**开发环境：**
```bash
mvn spring-boot:run -Dspring-boot.run.main-class=io.leavesfly.jtrade.demo.IntegrationDemo
```

**生产环境：**
```bash
# 解压分发包
tar -xzf jtrade-1.0.0-distribution.tar.gz
cd jtrade-1.0.0

# 配置环境变量
export OPENAI_API_KEY=your_key
export FINNHUB_API_KEY=your_key
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# 运行CLI
./bin/jtrade.sh cli
```

## 📝 注意事项

1. 确保安装了JDK 17或更高版本
2. 设置足够的堆内存（推荐至少2GB）
3. 配置必要的API密钥
4. 首次运行可能需要下载数据，请耐心等待

## 🔗 更多信息

- 完整文档: 查看`PROJECT_STATUS.md`
- 演示程序: 7个完整的Demo可供参考
- 源代码: `src/main/java/io/leavesfly/jtrade/`
