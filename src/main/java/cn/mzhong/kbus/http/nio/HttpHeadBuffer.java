package cn.mzhong.kbus.http.nio;

import java.io.ByteArrayOutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/29 11:32
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpHeadBuffer {
    private ByteArrayOutputStream headStream = new ByteArrayOutputStream();
    private byte[] spare;
    private byte[] buf = new byte[4];
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


    public void setSpare(byte[] spare) {
        this.spare = spare;
    }

    public byte[] getSpare() {
        return spare;
    }

    public boolean isEof() {
        return eof;
    }

    public void clear() {
        this.eof = false;
        this.buf = new byte[4];
        this.headStream = new ByteArrayOutputStream();
    }
}
