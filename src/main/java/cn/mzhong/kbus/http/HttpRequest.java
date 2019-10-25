package cn.mzhong.kbus.http;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:09
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequest {
    private final byte[] requestLineBytes;
    private final byte[][] headerBytes;
    private final byte[] contentBytes;
    private final HttpRequestLine requestLine;
    private final HttpRequestHeader header;
    private final InputStream inputStream;
    private final OutputStream outputStream;


    public HttpRequest(byte[] requestLineBytes, byte[][] headerBytes, byte[] contentBytes,
                       HttpRequestHeader httpHeader, InputStream inputStream, OutputStream outputStream) {
        String[] split = new String(requestLineBytes).split(" ");
        this.requestLine = new HttpRequestLine(split[0], split[1], split[3]);
        this.requestLineBytes = requestLineBytes;
        this.headerBytes = headerBytes;
        this.contentBytes = contentBytes;
        this.header = httpHeader;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public HttpRequestLine getRequestLine() {
        return requestLine;
    }

    public byte[] getRequestLineBytes() {
        return requestLineBytes;
    }

    public byte[][] getHeaderBytes() {
        return headerBytes;
    }

    public byte[] getContentBytes() {
        return contentBytes;
    }

    public HttpRequestHeader getHeader() {
        return header;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
