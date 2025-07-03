package cn.encmys.ykdz.forest.hyphascript.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    /**
     * 从文件中读取 UTF-8 编码的纯文本内容。
     *
     * @param file 要读取的文件
     * @return 文件内容的字符串表示
     * @throws IOException 如果文件读取失败
     */
    public static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();

        // 使用 BufferedReader 和 UTF-8 编码读取文件
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        return content.toString();
    }
}
