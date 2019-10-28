package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 15:55
 *
 * @author mzhong
 * @version 1.0
 */
public class OffChunkedTransferEncodingHttpResponseWriter implements HttpResponseWriter {

    @Override
    public void write(HttpUpstream upstream, HttpResponse response, HttpDownStream downStream, Location location) throws IOException {
        OutputStream outputStream = downStream.getOutputStream();
        //-------------- 写响应行 --------------
        outputStream.write(response.getResponseLine().getLineBytes());
        outputStream.write(HttpConstant.LINE_SEPARATOR);

        //-------------- 写header、content --------------
        HttpHeader header = response.getHeader();
        InputStream upstreamIn = upstream.getInputStream();
        Integer contentLength = header.getInteger(HttpHeader.CONTENT_LENGTH);
        boolean isTransferEncoding = header.contains(HttpHeader.TRANSFER_ENCODING);
        // 传输响应体，响应体有两种。
        // 一种是header中有Content-Length字段的可以直接读取Content-Length大小的数据即可
        // 还有一种响应体是分块格式的数据，用Transfer-Encoding字段辨识
        if (isTransferEncoding) {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            // 读数据块
            byte[] lineBytes;
            while ((lineBytes = StreamUtils.readLine(upstreamIn)) != null) {
                String line = new String(lineBytes);
                int length = Integer.parseInt(line, 16);
                if (length == 0) {
                    // 丢掉结尾空行
                    StreamUtils.read(upstreamIn, 2);
                    break;
                }
                byte[] blockData = StreamUtils.read(upstreamIn, length);
                if (blockData != null) {
                    contentStream.write(blockData, 0, length);
                }
                // 丢掉换行符
                StreamUtils.read(upstreamIn, 2);
            }
            // 更改响应头
            header.remove(HttpHeader.TRANSFER_ENCODING)
                    .set(HttpHeader.CONTENT_LENGTH, String.valueOf(contentStream.size()));
            // 写响应头
            outputStream.write(header.toBytes());
            // 空行
            outputStream.write(HttpConstant.LINE_SEPARATOR);
            // flush一下，让客户端率先知道请求头
            outputStream.flush();
            // 写内容
            contentStream.writeTo(outputStream);
        } else if (contentLength != null && contentLength > 0) {
            outputStream.write(header.toBytes());
            outputStream.write(HttpConstant.LINE_SEPARATOR);
            StreamUtils.copyAt(upstreamIn, outputStream, contentLength);
        }
        outputStream.flush();
    }
}
