package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.ByteUtils;

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

    public byte[] toByteArray() {
        return ByteUtils.merge(responseLine.toByteArray(),
                HttpConstant.LINE_SEPARATOR,
                header.toByteArray(),
                HttpConstant.LINE_SEPARATOR,
                HttpConstant.LINE_SEPARATOR);
    }
}
