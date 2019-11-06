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
public class UpstreamHandler {
    private HttpHeadBuffer httpHeadBuffer = new HttpHeadBuffer();
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    private boolean headEof = false;

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
                }
            }
        }
    }

    private void handleBodyRead(SocketChannel channel) {

    }
}
