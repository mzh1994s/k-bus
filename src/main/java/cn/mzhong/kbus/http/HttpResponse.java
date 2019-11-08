package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:54
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpResponse {

    private final HttpResponseHead head;

    public HttpResponse(HttpResponseHead head) {
        this.head = head;
    }

    public HttpResponseLine getResponseLine() {
        return head.getResponseLine();
    }

    public HttpHeader getHeader() {
        return head.getHeader();
    }

    public HttpResponseHead getHead() {
        return head;
    }
}
