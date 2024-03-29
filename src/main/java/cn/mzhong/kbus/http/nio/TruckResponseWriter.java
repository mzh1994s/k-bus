package cn.mzhong.kbus.http.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 11:52
 *
 * @author mzhong
 * @version 1.0
 */
public class TruckResponseWriter extends AbstractResponseWriter {

    private byte[] eb = new byte[7];
    private long write;

    public TruckResponseWriter(HttpNioContext context) {
        super(context);
    }

    @Override
    public IOStatus writeBody(ByteBuffer buffer) throws IOException {
        boolean eof = false;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            byteArrayOutputStream.write(b);
            eb[0] = eb[1];
            eb[1] = eb[2];
            eb[2] = eb[3];
            eb[3] = eb[4];
            eb[4] = eb[5];
            eb[5] = eb[6];
            eb[6] = b;
            if (eb[0] == '\r' && eb[1] == '\n'
                    && eb[2] == '0'
                    && eb[3] == '\r' && eb[4] == '\n' && eb[5] == '\r' && eb[6] == '\n') {
                eof = true;
            }
        }
        ByteBuffer contentBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
        while (contentBuffer.hasRemaining()) {
            write += context.getDownstream().write(contentBuffer);
        }
        return eof ? IOStatus.EOF : IOStatus.MISSION;
    }
}
