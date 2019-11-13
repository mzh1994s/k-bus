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

    public SimpleResponseWriter(HttpContext context, long contentLength) {
        super(context);
        this.contentLength = contentLength;
    }

    public IOStatus writeBody(ByteBuffer buffer) throws IOException {
        if (contentLength > 0) {
            write += context.getDownstream().write(buffer);
            if (write >= contentLength) {
                return IOStatus.EOF;
            }
            return IOStatus.MISSION;
        } else {
            return IOStatus.EOF;
        }
    }
}
