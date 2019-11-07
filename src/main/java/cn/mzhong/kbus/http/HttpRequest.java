package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:09
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequest {

    private final HttpRequestHead head;

    public HttpRequest(HttpRequestHead httpHead) {
        this.head = httpHead;
    }

    public HttpRequestLine getRequestLine() {
        return head.getRequestLine();
    }

    public HttpHeader getHeader() {
        return head.getHeader();
    }
}
