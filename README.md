# 文件的BASE64编码及网络传输


## 项目概述


本项目实现了一个基于 Java 的隐写术文件传输系统，能够在 BMP 图像文件中隐藏秘密信息并通过网络传输。系统分为客户端和服务器端两部分，支持文件上传、隐写处理和隐写信息提取功能。


## 技术架构


### 核心技术
- **隐写术实现**：基于最低有效位 (LSB) 替换技术，在 BMP 图像中隐藏文本信息
- **网络通信**：使用 Java Socket 实现客户端与服务器端的通信
- **多线程处理**：使用线程池处理多个客户端连接和并发任务
- **文件传输**：支持 Base64 编码的文件数据传输和 CRC32 校验


### 功能说明


#### 1.核心功能
- **信息隐藏**：在 BMP 图像中隐藏文本信息，支持 24 位非压缩 BMP 格式
- **信息提取**：从含隐写信息的 BMP 图像中提取隐藏的文本内容
- **隐写原理**：通过修改像素值的最低有效位 (LSB) 来存储二进制信息，不影响图像视觉效果


#### 2.文件传输功能
- **文件上传**：客户端向服务器上传任意格式文件
- **隐写处理**：服务器对上传的 BMP 文件自动进行隐写处理
- **完整性校验**：使用 CRC32 算法验证文件传输的完整性

### 代码结构说明代码结构说明


```javascript
public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        // 连接服务器
        // 启动响应处理线程
        // 处理用户输入
    }

    private static void uploadFile(String filePath, PrintWriter out) {
        // 文件上传实现
    }
}
```
### 文件结构
```
├── client/              # 客户端
│   ├── ClientMain.java  # 主逻辑
│   └── Steganography.java # 隐写提取
│   └── UserInteractionHandler.java # 客户端输入处理
├── server/              # 服务器
│   ├── ServerMain.java  # 服务启动
│   └── Steganography.java # 隐写嵌入
│   └── ServerMain.java 
└── 运行目录自动生成文件夹
    ├── server_files/     # 服务器存储
    └── received_files/   # 客户端存储
```

### 服务器配置
server_host=localhost

server_port=8888
