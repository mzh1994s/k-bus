package cn.mzhong.kbus.http.nio;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * TODO<br>
 * 创建时间： 2019/10/29 11:32
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpHeadBuffer {
    private ByteArrayOutputStream headStream = new ByteArrayOutputStream();
    private byte[] buf = new byte[4];
    private ByteBuffer byteBuffer;
    private boolean eof;

    public void add(Byte _byte) {
        if (!eof) {
            buf[0] = buf[1];
            buf[1] = buf[2];
            buf[2] = buf[3];
            buf[3] = _byte;
            if (buf[0] == '\r' && buf[1] == '\n' && buf[2] == '\r' && buf[3] == '\n') {
                eof = true;
            }
        }
        headStream.write(_byte);
    }

    public byte[] toBytes() {
        return headStream.toByteArray();
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public boolean isEof() {
        return eof;
    }

}
