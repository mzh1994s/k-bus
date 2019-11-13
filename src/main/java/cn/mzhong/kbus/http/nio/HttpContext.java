package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpResponse;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Http请求时的IO上下文对象，对象中的所有属性是对线程可见的，使用volatile修饰，
 * 在任意线程中大可放心访问。HttpContent对象在一个新的请求时创建，而在请求完成
 * 或者出错时应当被销毁。<br>
 * 创建时间： 2019/11/7 14:19
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpContext {

    private final HttpHeadReader requestHeadReader = new HttpHeadReader();

    public HttpHeadReader getRequestHeadReader() {
        return requestHeadReader;
    }

    private final HttpHeadReader responseHeadReader = new HttpHeadReader();

    public HttpHeadReader getResponseHeadReader() {
        return responseHeadReader;
    }

    /**
     * 入站缓冲区
     */
    private final ByteBuffer inboundBuffer = ByteBuffer.allocate(4096);

    public ByteBuffer getInboundBuffer() {
        return inboundBuffer;
    }

    /**
     * 出站缓冲区
     */
    private final ByteBuffer outboundBuffer = ByteBuffer.allocate(4096);

    public ByteBuffer getOutboundBuffer() {
        return outboundBuffer;
    }

    private volatile SocketChannel downstream;

    public SocketChannel getDownstream() {
        return downstream;
    }

    public void setDownstream(SocketChannel downstream) {
        this.downstream = downstream;
    }

    private volatile SocketChannel upstream;

    public SocketChannel getUpstream() {
        return upstream;
    }

    public void setUpstream(SocketChannel upstream) {
        this.upstream = upstream;
    }

    private volatile HttpRequest request;

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    private volatile HttpResponse response;

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    private volatile HttpWriter responseWriter;

    public HttpWriter getResponseWriter() {
        return responseWriter;
    }

    public void setResponseWriter(HttpWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    private volatile HttpWriter requestWriter;

    public HttpWriter getRequestWriter() {
        return requestWriter;
    }

    public void setRequestWriter(HttpWriter requestWriter) {
        this.requestWriter = requestWriter;
    }

    private volatile SelectionKey downstreamKey;

    public SelectionKey getDownstreamKey() {
        return downstreamKey;
    }

    public void setDownstreamKey(SelectionKey downstreamKey) {
        this.downstreamKey = downstreamKey;
    }

    private volatile SelectionKey upstreamKey;

    public SelectionKey getUpstreamKey() {
        return upstreamKey;
    }

    public void setUpstreamKey(SelectionKey upstreamKey) {
        this.upstreamKey = upstreamKey;
    }
}
