package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/6 17:08
 *
 * @author mzhong
 * @version 1.0
 */
public class ChannelHandler {
    private HttpHeadBuffer httpHeadBuffer = new HttpHeadBuffer();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private boolean headEof = false;
    private HttpNioConnector connector;
    private SocketChannel downstream;
    private SocketChannel upstream;

    public ChannelHandler(HttpNioConnector connector) {
        this.connector = connector;
    }

    public void handleDownStreamRead(SocketChannel channel) throws IOException {
        if (headEof) {
            this.handleBodyRead(channel);
        } else {
            this.handleHeadRead(channel);
        }
    }

    private void parseHead(byte[] bytes) {

    }

    private void handleHeadRead(SocketChannel channel) throws IOException {
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                httpHeadBuffer.add(buffer.get());
                if (httpHeadBuffer.isEof()) {
                    headEof = true;
                    httpHeadBuffer.setByteBuffer(buffer);
                    byte[] bytes = httpHeadBuffer.toBytes();
                    System.out.println(new String(bytes));
                }
            }
        }
    }

    private void handleBodyRead(SocketChannel channel) {

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

    public HttpHeadBuffer getHttpHeadBuffer() {
        return httpHeadBuffer;
    }

    public void setHttpHeadBuffer(HttpHeadBuffer httpHeadBuffer) {
        this.httpHeadBuffer = httpHeadBuffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}
