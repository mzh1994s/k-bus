package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpHeader;
import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpRequestLine;

import java.io.InputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/31 13:46
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioRequest extends HttpRequest {

    protected final InputStream inputStream;

    public HttpBioRequest(HttpRequestLine requestLine, HttpHeader header, InputStream inputStream) {
        super(requestLine, header);
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
