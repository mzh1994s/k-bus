package cn.mzhong.kbus.util;

import java.io.Closeable;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 16:40
 *
 * @author mzhong
 * @version 1.0
 */
public class StreamUtils {
    private StreamUtils() {
    }

    public void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }
}


