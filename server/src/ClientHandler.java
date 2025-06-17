import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.CRC32;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("客户端消息: " + clientMessage);

                String[] parts = clientMessage.split(" ", 2);
                String command = parts[0];
                String param = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "UPLOAD_FILE":
                        handleFileUpload(in);
                        break;
                    case "EXIT":
                        System.out.println("客户端断开连接");
                        return;
                    default:
                        out.println("未知命令: " + command);
                }
            }
        } catch (IOException e) {
            System.err.println("处理客户端连接时出错: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileUpload(BufferedReader in) throws IOException {
        try {
            // 读取文件名
            String fileName = in.readLine();
            System.out.println("接收文件: " + fileName);

            // 读取文件大小
            long fileSize = Long.parseLong(in.readLine());
            System.out.println("文件大小: " + fileSize + " 字节");

            // 读取Base64编码的文件内容
            String base64Data = in.readLine();
            byte[] fileData = Base64.getDecoder().decode(base64Data);

            // 读取CRC32校验值并验证
            String clientCrc = in.readLine();
            CRC32 crc32 = new CRC32();
            crc32.update(fileData);
            String serverCrc = String.valueOf(crc32.getValue());

            if (clientCrc.equals(serverCrc)) {
                // 保存文件
                String filePath = "server_files/" + fileName;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(fileData);
                    System.out.println("文件保存成功: " + filePath);

                    // 如果是BMP文件，执行隐写
                    if (fileName.toLowerCase().endsWith(".bmp")) {
                        try {
                            String secretMessage = "This is a hidden message from server";
                            String stegoFileName = "stego_" + fileName;
                            String stegoFilePath = "server_files/" + stegoFileName;

                            // 执行隐写
                            Steganography.hideMessage(filePath, stegoFilePath, secretMessage);
                            System.out.println("已对BMP文件完成隐写: " + stegoFilePath);

                            // 发送隐写文件给客户端
                            sendFileToClient(stegoFileName, stegoFilePath);
                        } catch (Exception e) {
                            System.err.println("隐写或发送文件失败: " + e.getMessage());
                        }
                    }
                }
            } else {
                System.err.println("CRC32校验失败，文件可能损坏");
            }
        } catch (Exception e) {
            System.err.println("处理文件上传时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendFileToClient(String fileName, String filePath) throws IOException {
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(filePath))) {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // 发送文件名
            out.println("FILE " + fileName);

            // 发送文件大小
            long fileSize = Files.size(Paths.get(filePath));
            out.println(fileSize);

            // 发送文件内容（Base64编码）
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            Base64.Encoder encoder = Base64.getEncoder();
            try (OutputStream encodedOut = encoder.wrap(byteOut)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    encodedOut.write(buffer, 0, bytesRead);
                }
            }
            out.println(byteOut.toString("UTF-8"));

            // 发送CRC32校验值
            CRC32 crc32 = new CRC32();
            crc32.update(byteOut.toByteArray());
            out.println(crc32.getValue());

            System.out.println("已发送隐写文件: " + fileName);
        }
    }
}