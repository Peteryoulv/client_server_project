import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private DatabaseManager databaseManager;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.databaseManager = new DatabaseManager();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                // 接收客户端消息并处理
                String[] parts = clientMessage.split(" ", 2);
                String command = parts[0];

                switch (command) {
                    case "UPLOAD_FILE":
                        handleFileUpload(in);
                        break;
                    case "UPLOAD_DATA":
                        handleDataUpload(parts[1]);
                        break;
                    case "CHECK_VERSION":
                        out.println("VERSION 1.0"); // 示例版本号
                        break;
                    default:
                        out.println("UNKNOWN_COMMAND");
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
        String fileName = in.readLine();
        String fileSizeStr = in.readLine();
        long fileSize = Long.parseLong(fileSizeStr);
        String base64Data = in.readLine();

        // 解码文件数据
        byte[] fileData = Base64.getDecoder().decode(base64Data);

        // 校验CRC32
        String clientCrc = in.readLine();
        CRC32 crc32 = new CRC32();
        crc32.update(fileData);
        String serverCrc = String.valueOf(crc32.getValue());

        if (clientCrc.equals(serverCrc)) {
            // 保存文件
            try (FileOutputStream fos = new FileOutputStream("server_files/" + fileName)) {
                fos.write(fileData);
            }

            // 如果是BMP文件，尝试隐写
            if (fileName.toLowerCase().endsWith(".bmp")) {
                try {
                    String secretMessage = "This is a hidden message"; // 示例隐写信息
                    Steganography.hideMessage("server_files/" + fileName,
                            "server_files/stego_" + fileName,
                            secretMessage);
                } catch (Exception e) {
                    System.err.println("隐写失败: " + e.getMessage());
                }
            }
        } else {
            System.err.println("CRC32校验失败");
        }
    }

    private void handleDataUpload(String base64Data) {
        String decodedData = new String(Base64.getDecoder().decode(base64Data), StandardCharsets.UTF_8);
        databaseManager.saveData(decodedData);
    }
}    