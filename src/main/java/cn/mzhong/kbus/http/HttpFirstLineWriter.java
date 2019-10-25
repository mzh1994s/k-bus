package cn.mzhong.kbus.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:13
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpFirstLineWriter implements HttpWriter {

    @Override
    public void write(HttpRequest httpRequest, HttpUpstream httpUpstream, Location location) throws IOException {
        OutputStream upstreamOut = httpUpstream.getOutputStream();
        upstreamOut.write(httpRequest.getRequestLineBytes());
        upstreamOut.flush();
    }
}
