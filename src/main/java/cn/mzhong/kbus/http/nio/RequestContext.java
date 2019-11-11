package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpResponse;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 14:19
 *
 * @author mzhong
 * @version 1.0
 */
public class RequestContext {

    private final HttpHeadBuffer requestHeadBuffer = new HttpHeadBuffer();

    public HttpHeadBuffer getRequestHeadBuffer() {
        return requestHeadBuffer;
    }

    private final HttpHeadBuffer responseHeadBuffer = new HttpHeadBuffer();

    public HttpHeadBuffer getResponseHeadBuffer() {
        return responseHeadBuffer;
    }

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    private SocketChannel downstream;

    public SocketChannel getDownstream() {
        return downstream;
    }

    public void setDownstream(SocketChannel downstream) {
        this.downstream = downstream;
    }

    private SocketChannel upstream;

    public SocketChannel getUpstream() {
        return upstream;
    }

    public void setUpstream(SocketChannel upstream) {
        this.upstream = upstream;
    }

    private HttpRequest request;

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    private HttpResponse response;

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }


    private ResponseWriter responseWriter;

    public ResponseWriter getResponseWriter() {
        return responseWriter;
    }

    public void setResponseWriter(ResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    private SelectionKey downstreamKey;

    public SelectionKey getDownstreamKey() {
        return downstreamKey;
    }

    public void setDownstreamKey(SelectionKey downstreamKey) {
        this.downstreamKey = downstreamKey;
    }

    private SelectionKey upstreamKey;

    public SelectionKey getUpstreamKey() {
        return upstreamKey;
    }

    public void setUpstreamKey(SelectionKey upstreamKey) {
        this.upstreamKey = upstreamKey;
    }

    private boolean requested;

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }
}
