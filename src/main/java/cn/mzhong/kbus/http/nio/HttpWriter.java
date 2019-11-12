package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 11:17
 *
 * @author mzhong
 * @version 1.0
 */
public interface HttpWriter {

    void writeHead(ByteBuffer buffer) throws IOException;

    IOStatus writeBody(ByteBuffer buffer) throws IOException;
}
