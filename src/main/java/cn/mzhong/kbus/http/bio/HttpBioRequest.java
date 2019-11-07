package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpRequestHead;
import cn.mzhong.kbus.http.HttpRequest;

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

    public HttpBioRequest(HttpRequestHead httpHead, InputStream inputStream) {
        super(httpHead);
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
