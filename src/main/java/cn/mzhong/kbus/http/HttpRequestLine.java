package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:57
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequestLine {

    private final byte[] lineBytes;
    private final String line;

    private final String method;
    private final String uri;
    private final String version;

    public HttpRequestLine(byte[] lineBytes, String line, String method, String uri, String version) {
        this.lineBytes = lineBytes;
        this.line = line;
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public byte[] getLineBytes() {
        return lineBytes;
    }

    public String getLine() {
        return line;
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
        return this.line;
    }

    public static HttpRequestLine parse(byte[] lineBytes) {
        String line = new String(lineBytes);
        String[] split = line.split(" ");
        return new HttpRequestLine(lineBytes, line, split[0], split[1], split[2]);
    }
}
