package cn.mzhong.kbus.http;

import java.nio.charset.StandardCharsets;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:57
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequestLine {

    private final String method;
    private final String uri;
    private final String version;

    public HttpRequestLine(byte[] lineBytes, String line, String method, String uri, String version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public HttpRequestLine(String method, String uri, String version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public byte[] toByteArray() {
        return getLine().getBytes(StandardCharsets.ISO_8859_1);
    }

    public String getLine() {
        return method + " " + uri + " " + version;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return getLine();
    }

    public static HttpRequestLine parse(byte[] lineBytes) {
        return parse(new String(lineBytes));
    }

    public static HttpRequestLine parse(String line) {
        String[] split = line.split(" ");
        return new HttpRequestLine(split[0], split[1], split[2]);
    }
}
