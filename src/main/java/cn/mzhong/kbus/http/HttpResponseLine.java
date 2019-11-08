package cn.mzhong.kbus.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:50
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpResponseLine {

    private String version;
    private String statusCode;
    private String statusName;

    private HttpResponseLine() {
    }

    public String getLine() {
        return version + " " + statusCode + " " + statusName;
    }

    public String getVersion() {
        return version;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusName() {
        return statusName;
    }

    @Override
    public String toString() {
        return getLine();
    }

    public static HttpResponseLine parse(String line) throws IOException {
        HttpResponseLine responseLine = new HttpResponseLine();
        // 读version
        int index = line.indexOf(' ');
        if (index < 0) {
            throw new IOException("读取HTTP版本失败：" + responseLine);
        }
        responseLine.version = line.substring(0, index);

        // 读状态码
        // 跳过之前的空格
        index++;
        if (index > line.length()) {
            throw new IOException("读取HTTP状态码失败，响应行长度不够：" + responseLine);
        }
        String subResponseLine = line.substring(index);
        index = subResponseLine.indexOf(' ');
        if (index < 0) {
            throw new IOException("读取HTTP状态码失败，无法解析：" + responseLine);
        }
        responseLine.statusCode = subResponseLine.substring(0, index);
        // 跳过空行
        index++;
        if (index > subResponseLine.length()) {
            throw new IOException("读取HTTP状态名称失败，响应行长度不够：" + responseLine);
        }
        responseLine.statusName = subResponseLine.substring(index);
        return responseLine;
    }

    public static HttpResponseLine parse(byte[] lineBytes) throws IOException {
        return parse(new String(lineBytes));
    }

    public byte[] toByteArray() {
        return getLine().getBytes(StandardCharsets.ISO_8859_1);
    }
}
