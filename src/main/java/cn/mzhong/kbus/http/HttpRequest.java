package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:09
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequest {
    protected final HttpRequestLine requestLine;
    protected final HttpHeader header;

    public HttpRequest(HttpRequestLine requestLine, HttpHeader header) {
        this.requestLine = requestLine;
        this.header = header;
    }

    public HttpRequestLine getRequestLine() {
        return requestLine;
    }

    public HttpHeader getHeader() {
        return header;
    }
}
