package cn.mzhong.kbus.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:46
 *
 * @author mzhong
 * @version 1.0
 */
public class SimpleHttpRequestWriter implements HttpRequestWriter {

    @Override
    public void write(HttpRequest httpRequest, HttpUpstream httpUpstream, Location location) throws IOException {
        OutputStream outputStream = httpUpstream.getOutputStream();
        // 写请求行
        outputStream.write(httpRequest.getRequestLine().getLineBytes());
        outputStream.write(HttpConstant.LINE_SEPARATOR);
        // 请求头
        HttpHeader header = httpRequest.getHeader();
        header.set("Host", location.getHost());
        outputStream.write(httpRequest.getHeader().toBytes());
        // 空行
        outputStream.write(HttpConstant.LINE_SEPARATOR);
        // 请求头体
        byte[] contentBytes = httpRequest.getContent();
        if (contentBytes != null) {
            outputStream.write(contentBytes);
        }
        outputStream.flush();
    }
}
