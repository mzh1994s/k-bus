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
public class HttpNioContext {

    private final HttpHeadReader requestHeadReader = new HttpHeadReader();

    private final HttpHeadReader responseHeadReader = new HttpHeadReader();
    /**
     * 入站缓冲区
     */
    private final ByteBuffer inboundBuffer = ByteBuffer.allocate(4096);
    /**
     * 出站缓冲区
     */
    private final ByteBuffer outboundBuffer = ByteBuffer.allocate(4096);

    private volatile SocketChannel downstream;

    private volatile SocketChannel upstream;

    private volatile HttpRequest request;

    private volatile HttpResponse response;

    private volatile HttpWriter responseWriter;

    private volatile HttpWriter requestWriter;

    private volatile SelectionKey downstreamKey;

    private volatile SelectionKey upstreamKey;

    public HttpHeadReader getRequestHeadReader() {
        return requestHeadReader;
    }

    public HttpHeadReader getResponseHeadReader() {
        return responseHeadReader;
    }

    public ByteBuffer getInboundBuffer() {
        return inboundBuffer;
    }

    public ByteBuffer getOutboundBuffer() {
        return outboundBuffer;
    }

    public SocketChannel getDownstream() {
        return downstream;
    }

    public void setDownstream(SocketChannel downstream) {
        this.downstream = downstream;
    }

    public SocketChannel getUpstream() {
        return upstream;
    }

    public void setUpstream(SocketChannel upstream) {
        this.upstream = upstream;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public HttpWriter getResponseWriter() {
        return responseWriter;
    }

    public void setResponseWriter(HttpWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    public HttpWriter getRequestWriter() {
        return requestWriter;
    }

    public void setRequestWriter(HttpWriter requestWriter) {
        this.requestWriter = requestWriter;
    }

    public SelectionKey getDownstreamKey() {
        return downstreamKey;
    }

    public void setDownstreamKey(SelectionKey downstreamKey) {
        this.downstreamKey = downstreamKey;
    }

    public SelectionKey getUpstreamKey() {
        return upstreamKey;
    }

    public void setUpstreamKey(SelectionKey upstreamKey) {
        this.upstreamKey = upstreamKey;
    }
}
