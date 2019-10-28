package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:54
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpResponse {

    private HttpResponseLine responseLine;
    private HttpHeader header;

    public HttpResponse(HttpResponseLine responseLine, HttpHeader header) {
        this.responseLine = responseLine;
        this.header = header;
    }

    public HttpResponseLine getResponseLine() {
        return responseLine;
    }

    public HttpHeader getHeader() {
        return header;
    }
}
