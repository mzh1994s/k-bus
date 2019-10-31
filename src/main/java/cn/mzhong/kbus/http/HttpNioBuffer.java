package cn.mzhong.kbus.http;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO<br>
 * 创建时间： 2019/10/29 11:32
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioBuffer {
    private List<Byte> data = new LinkedList<Byte>();
    private ByteBuffer byteBuffer;

    public HttpNioBuffer(int bufferSize) {
        byteBuffer = ByteBuffer.allocate(bufferSize);
    }

    /**
     * TODO
     * 创建时间： 2019/10/29 16:30
     *
     * @author mzhong
     * @version 1.0
     */
    public void add(ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        for (int i = 0; i < position; i++) {
            data.add(byteBuffer.get());
        }
    }

    public Byte[] toBytes() {
        return this.data.toArray(new Byte[this.data.size()]);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
