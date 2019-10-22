package cn.mzhong.kbus.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 10:04
 *
 * @author mzhong
 * @version 1.0
 */
public interface HttpConnector {

    void connect(HttpRequest request, String host, int port) throws IOException;
}
