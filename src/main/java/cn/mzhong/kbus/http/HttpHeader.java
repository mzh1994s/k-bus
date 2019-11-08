package cn.mzhong.kbus.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 10:26
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpHeader {

    public final static String CONTENT_LENGTH = "Content-Length";
    public final static String TRANSFER_ENCODING = "Transfer-Encoding";
    public final static String CONNECTION = "Connection";

    private Map<String, String> data = new HashMap<>();

    public HttpHeader() {
    }

    public Integer getInteger(String key) {
        String value = data.get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public int getIntValue(String key) {
        Integer integer = getInteger(key);
        return integer == null ? 0 : integer;
    }

    public String getString(String key) {
        return data.get(key);
    }

    public HttpHeader set(String key, String value) {
        data.put(key, value);
        return this;
    }

    public HttpHeader remove(String key) {
        data.remove(key);
        return this;
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public void putLine(byte[] lineBytes) {
        this.putLine(new String(lineBytes, StandardCharsets.ISO_8859_1));
    }

    public void putLine(String line) {
        int index = line.indexOf(':');
        if (index < 0) {
            return;
        }
        String headerName = line.substring(0, index);
        String headerValue = line.substring(index + 1).trim();
        this.data.put(headerName, headerValue);
    }

    public byte[] toByteArray() {
        return toString().getBytes();
    }

    @Override
    public String toString() {
        StringBuilder headerBuilder = new StringBuilder();
        Set<Map.Entry<String, String>> entries = data.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            headerBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        return headerBuilder.toString();
    }

    public static HttpHeader parse(byte[][] linesBytes) throws IOException {
        HttpHeader httpHeader = new HttpHeader();
        for (byte[] lineBytes : linesBytes) {
            httpHeader.putLine(lineBytes);
        }
        return httpHeader;
    }

    public static HttpHeader parse(byte[] linesBytes) throws IOException {
        return parse(new String(linesBytes, StandardCharsets.ISO_8859_1));
    }

    public static HttpHeader parse(String linesString) throws IOException {
        return parse(linesString.split("\r\n"), 0);
    }

    public static HttpHeader parse(String[] lines, int offset) throws IOException {
        HttpHeader httpHeader = new HttpHeader();
        int length = lines.length;
        for (int i = offset; i < length; i++) {
            httpHeader.putLine(lines[i]);
        }
        return httpHeader;
    }
}
