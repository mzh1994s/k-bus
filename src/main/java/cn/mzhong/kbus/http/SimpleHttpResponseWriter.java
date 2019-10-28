package cn.mzhong.kbus.http;

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
public class SimpleHttpResponseWriter implements HttpResponseWriter {

    @Override
    public void write(HttpUpstream upstream, HttpResponse response, HttpDownStream downStream, Location location) throws IOException {
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
            // 读数据块
            byte[] eof = new byte[5];
            int read;
            while ((read = upstreamIn.read()) != -1) {
                eof[4] = (byte) read;
                outputStream.write(eof[4]);
                if (eof[0] == '0'
                        && eof[1] == '\r'
                        && eof[2] == '\n'
                        && eof[3] == '\r'
                        && eof[4] == '\n') {
                    break;
                }
                eof[0] = eof[1];
                eof[1] = eof[2];
                eof[2] = eof[3];
                eof[3] = eof[4];
            }
        } else if (contentLength != null && contentLength > 0) {
            StreamUtils.copyAt(upstreamIn, outputStream, contentLength);
        }
        outputStream.flush();
    }
}
