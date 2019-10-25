package cn.mzhong.kbus.http;

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

    public HttpRequestLine(String method, String uri, String version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
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
}
