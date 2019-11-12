package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 11:52
 *
 * @author mzhong
 * @version 1.0
 */
public abstract class AbstractResponseWriter implements HttpWriter {

    protected RequestContext context;

    public AbstractResponseWriter(RequestContext context) {
        this.context = context;
    }

    @Override
    public void writeHead(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            context.getDownstream().write(buffer);
        }
    }
}
