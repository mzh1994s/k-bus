package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/12 10:49
 *
 * @author mzhong
 * @version 1.0
 */
public class RequestWriter implements HttpWriter {

    private SocketChannel upstream;
    private long contentLength;
    private long write;

    public RequestWriter(RequestContext context, long contentLength) {
        upstream = context.getUpstream();
        this.contentLength = contentLength;
    }

    @Override
    public void writeHead(ByteBuffer buffer) throws IOException {
        upstream.write(buffer);
    }

    @Override
    public int writeBody(ByteBuffer buffer) throws IOException {
        int writeLen = -1;
        if (contentLength > 0) {
            writeLen = upstream.write(buffer);
            write += writeLen;
            if (write >= contentLength) {
                return -1;
            }
        }
        return writeLen;
    }
}
