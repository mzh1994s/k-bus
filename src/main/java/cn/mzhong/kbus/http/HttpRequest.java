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
    private String method;
    private String uri;
    private String version;
    private HttpHeader httpHeader;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] data;

    public HttpRequest(String method, String uri, String version, HttpHeader httpHeader, byte[] data, InputStream inputStream, OutputStream outputStream) {
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.httpHeader = httpHeader;
        this.data = data;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public HttpHeader getHttpHeader() {
        return httpHeader;
    }

    public byte[] getData() {
        return data;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
