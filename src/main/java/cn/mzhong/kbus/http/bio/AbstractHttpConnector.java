package cn.mzhong.kbus.http.bio;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 16:59
 *
 * @author mzhong
 * @version 1.0
 */
public abstract class AbstractHttpConnector {
    /**
     * 规定如何向上游服务器发送消息部分
     */
    protected HttpBioRequestWriter requestWriter;

    protected HttpBioResponseWriter responseWriter;

    public void setRequestWriter(HttpBioRequestWriter requestWriter) {
        this.requestWriter = requestWriter;
    }

    public void setResponseWriter(HttpBioResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }
}
