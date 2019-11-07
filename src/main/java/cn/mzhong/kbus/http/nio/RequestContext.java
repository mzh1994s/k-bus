package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpResponse;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 14:19
 *
 * @author mzhong
 * @version 1.0
 */
public class RequestContext {
    private final HttpHeadBuffer headBuffer = new HttpHeadBuffer();
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private boolean requested;
    private SocketChannel downstream;
    private SocketChannel upstream;
    private HttpRequest request;
    private HttpResponse response;

    public HttpHeadBuffer getHeadBuffer() {
        return headBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
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
}
