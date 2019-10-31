package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpConstant;
import cn.mzhong.kbus.http.HttpHeader;
import cn.mzhong.kbus.http.HttpResponse;
import cn.mzhong.kbus.http.Location;
import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 13:38
 *
 * @author mzhong
 * @version 1.0
 */
public class SimpleHttpBioResponseWriter implements HttpBioResponseWriter {

    @Override
    public void write(HttpBioUpstream upstream, HttpResponse response, HttpBioDownStream downStream, Location location) throws IOException {
        OutputStream outputStream = downStream.getOutputStream();
        //-------------- 写响应行 --------------
        outputStream.write(response.getResponseLine().getLineBytes());
        outputStream.write(HttpConstant.LINE_SEPARATOR);

        //-------------- 写响应header --------------
        HttpHeader header = response.getHeader();
        outputStream.write(header.toBytes());
        outputStream.write(HttpConstant.LINE_SEPARATOR);
        // flush一下，让客户端率先知道请求头
        outputStream.flush();

        //-------------- 写content --------------
        InputStream upstreamIn = upstream.getInputStream();
        Integer contentLength = header.getInteger(HttpHeader.CONTENT_LENGTH);
        boolean isTransferEncoding = header.contains(HttpHeader.TRANSFER_ENCODING);
        // 传输响应体，响应体有两种。
        // 一种是header中有Content-Length字段的可以直接读取Content-Length大小的数据即可
        // 还有一种响应体是分块格式的数据，用Transfer-Encoding字段辨识
        if (isTransferEncoding) {
            byte[] bytes;
            while ((bytes = StreamUtils.readLine(upstreamIn)) != null) {
                outputStream.write(bytes);
                outputStream.write(HttpConstant.LINE_SEPARATOR);
                String line = new String(bytes);
                int length = Integer.parseInt(line, 16);
                if (length == 0) {
                    StreamUtils.read(upstreamIn, 2);
                    outputStream.write(HttpConstant.LINE_SEPARATOR);
                    break;
                }
                bytes = StreamUtils.read(upstreamIn, length);
                if (bytes != null) {
                    StreamUtils.read(upstreamIn, 2);
                    outputStream.write(bytes);
                }
                outputStream.write(HttpConstant.LINE_SEPARATOR);
            }
        } else if (contentLength != null && contentLength > 0) {
            StreamUtils.copyAt(upstreamIn, outputStream, contentLength);
        }
        outputStream.flush();
    }
}
