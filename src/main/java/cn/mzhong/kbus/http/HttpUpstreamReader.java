package cn.mzhong.kbus.http;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 13:53
 *
 * @author mzhong
 * @version 1.0
 */
public interface HttpUpstreamReader {

    boolean read(InputStream inputStream, OutputStream outputStream);
}
