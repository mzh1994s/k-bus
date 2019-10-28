package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:09
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequest {
    private final HttpRequestLine requestLine;
    private final HttpHeader header;
    private final byte[] content;

    public HttpRequest(HttpRequestLine requestLine, HttpHeader header, byte[] content) {
        this.requestLine = requestLine;
        this.header = header;
        this.content = content;
    }

    public HttpRequestLine getRequestLine() {
        return requestLine;
    }

    public HttpHeader getHeader() {
        return header;
    }

    public byte[] getContent() {
        return content;
    }
}
