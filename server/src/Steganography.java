import java.io.*;

public class Steganography {
    public static void hideMessage(String inputImagePath, String outputImagePath, String message) throws Exception {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputImagePath));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputImagePath))) {

            // 读取BMP文件头 (14 bytes) 和信息头 (40 bytes)
            byte[] fileHeader = new byte[14];
            bis.read(fileHeader);

            byte[] infoHeader = new byte[40];
            bis.read(infoHeader);

            // 写入文件头和信息头
            bos.write(fileHeader);
            bos.write(infoHeader);

            // 获取图像数据偏移量
            int dataOffset = ((fileHeader[13] & 0xFF) << 24) |
                    ((fileHeader[12] & 0xFF) << 16) |
                    ((fileHeader[11] & 0xFF) << 8) |
                    (fileHeader[10] & 0xFF);

            // 跳过调色板
            int paletteSize = dataOffset - 54;
            byte[] palette = new byte[paletteSize];
            bis.read(palette);
            bos.write(palette);

            // 准备要隐藏的消息
            message += "\0"; // 添加结束标记
            byte[] messageBytes = message.getBytes("UTF-8");
            int messageLength = messageBytes.length;

            // 写入消息长度
            for (int i = 0; i < 4; i++) {
                int byteValue = bis.read();
                byteValue = (byteValue & 0xFE) | ((messageLength >> (i * 8)) & 0x01);
                bos.write(byteValue);
            }

            // 写入消息内容
            int messageIndex = 0;
            int bitIndex = 0;

            while (messageIndex < messageLength) {
                int byteValue = bis.read();
                if (byteValue == -1) break;

                if (bitIndex < 8) {
                    // 修改最低有效位
                    byte currentMessageByte = messageBytes[messageIndex];
                    int bit = (currentMessageByte >> bitIndex) & 0x01;
                    byteValue = (byteValue & 0xFE) | bit;
                    bitIndex++;

                    if (bitIndex == 8) {
                        messageIndex++;
                        bitIndex = 0;
                    }
                }

                bos.write(byteValue);
            }

            // 复制剩余的图像数据
            int b;
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }
        }
    }
}