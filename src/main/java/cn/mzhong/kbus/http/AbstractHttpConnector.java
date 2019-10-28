package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 16:59
 *
 * @author mzhong
 * @version 1.0
 */
public abstract class AbstractHttpConnector implements HttpConnector {
    /**
     * 规定如何向上游服务器发送消息部分
     */
    protected HttpRequestWriter requestWriter;

    protected HttpResponseWriter responseWriter;

    public void setRequestWriter(HttpRequestWriter requestWriter) {
        this.requestWriter = requestWriter;
    }

    public void setResponseWriter(HttpResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }
}
