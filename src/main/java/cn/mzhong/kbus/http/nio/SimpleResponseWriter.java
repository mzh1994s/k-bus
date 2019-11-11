package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 11:38
 *
 * @author mzhong
 * @version 1.0
 */
public class SimpleResponseWriter extends AbstractResponseWriter {

    private long write;
    private long contentLength;

    public SimpleResponseWriter(RequestContext context, long contentLength) {
        super(context);
        this.contentLength = contentLength;
    }

    public int writeBody(ByteBuffer buffer) throws IOException {
        write += buffer.limit() - buffer.position();
        while (buffer.hasRemaining()) {
            context.getDownstream().write(buffer);
        }
        System.out.println(context.getRequest().getRequestLine().getUri() + ":" + write + "/" + contentLength);
        if (write >= contentLength) {
            return -1;
        }
        return 1;
    }
}
