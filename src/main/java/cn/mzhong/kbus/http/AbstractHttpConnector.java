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
     * 规定如何向上游服务器发送请求方法URI协议/版本
     */
    protected HttpWriter firstLineWriter;

    /**
     * 规定如何向上游服务器发送请求头信息
     */
    protected HttpWriter headerWriter;
    /**
     * 规定如何向上游服务器发送消息部分
     */
    protected HttpWriter contentWriter;

    protected HttpReader<Boolean> firstLineReader;

    protected HttpReader<HttpResponseHeader> headerReader;

    protected HttpReader<Boolean> contentReader;

    public void setFirstLineWriter(HttpWriter firstLineWriter) {
        this.firstLineWriter = firstLineWriter;
    }

    public void setHeaderWriter(HttpWriter headerWriter) {
        this.headerWriter = headerWriter;
    }

    public void setContentWriter(HttpWriter contentWriter) {
        this.contentWriter = contentWriter;
    }

    public void setFirstLineReader(HttpReader firstLineReader) {
        this.firstLineReader = firstLineReader;
    }

    public void setHeaderReader(HttpReader headerReader) {
        this.headerReader = headerReader;
    }

    public void setContentReader(HttpReader contentReader) {
        this.contentReader = contentReader;
    }
}
