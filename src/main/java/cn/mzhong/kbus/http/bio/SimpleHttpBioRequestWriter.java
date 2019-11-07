package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpConstant;
import cn.mzhong.kbus.http.HttpHeader;
import cn.mzhong.kbus.http.Location;
import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:46
 *
 * @author mzhong
 * @version 1.0
 */
public class SimpleHttpBioRequestWriter implements HttpBioRequestWriter {

    @Override
    public void write(HttpBioRequest httpRequest, HttpBioUpstream httpUpstream, Location location) throws IOException {
        OutputStream outputStream = httpUpstream.getOutputStream();
        // 写请求行
        outputStream.write(httpRequest.getRequestLine().toByteArray());
        outputStream.write(HttpConstant.LINE_SEPARATOR);
        // 请求头
        HttpHeader header = httpRequest.getHeader();
        header.set("Host", location.getHost());
        outputStream.write(httpRequest.getHeader().toByteArray());
        // 空行
        outputStream.write(HttpConstant.LINE_SEPARATOR);
        // 请求头体
        int contentLength = header.getIntValue(HttpHeader.CONTENT_LENGTH);
        StreamUtils.copyAt(httpRequest.getInputStream(), outputStream, contentLength);
        outputStream.flush();
    }
}
