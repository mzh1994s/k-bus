package cn.mzhong.kbus.http;

import cn.mzhong.kbus.http.nio.HttpHeadReader;
import cn.mzhong.kbus.util.ByteUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 16:50
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpResponseHead {
    private final HttpResponseLine responseLine;
    private final HttpHeader header;
    private ByteBuffer buffer;

    public HttpResponseHead(HttpResponseLine responseLine, HttpHeader header) {
        this.responseLine = responseLine;
        this.header = header;
    }

    public HttpResponseLine getResponseLine() {
        return responseLine;
    }

    public HttpHeader getHeader() {
        return header;
    }

    public ByteBuffer getBuffer() {
        if (buffer == null) {
            buffer = ByteBuffer.wrap(toByteArray());
        }
        return buffer;
    }

    public byte[] toByteArray() {
        return ByteUtils.merge(responseLine.toByteArray(),
                HttpConstant.LINE_SEPARATOR,
                header.toByteArray(),
                HttpConstant.LINE_SEPARATOR);
    }

    public static HttpResponseHead parse(HttpHeadReader httpHeadBuf) throws IOException {
        String headString = new String(httpHeadBuf.toBytes(), StandardCharsets.ISO_8859_1);
        String[] split = headString.split("\r\n");
        HttpResponseLine responseLine = HttpResponseLine.parse(split[0]);
        HttpHeader header = HttpHeader.parse(split, 1);
        return new HttpResponseHead(responseLine, header);
    }
}
