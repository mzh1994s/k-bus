package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.ByteUtils;

import java.nio.ByteBuffer;

/**
 * 这里的Head包括请求行和header<br>
 * 创建时间： 2019/11/7 16:44
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequestHead {
    private final HttpRequestLine requestLine;
    private final HttpHeader header;
    private ByteBuffer buffer;

    public HttpRequestHead(HttpRequestLine requestLine, HttpHeader header) {
        this.requestLine = requestLine;
        this.header = header;
    }

    public HttpRequestLine getRequestLine() {
        return requestLine;
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
        return ByteUtils.merge(requestLine.toByteArray(),
                HttpConstant.LINE_SEPARATOR,
                header.toByteArray(),
                HttpConstant.LINE_SEPARATOR);
    }
}
