package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpHeader;
import cn.mzhong.kbus.http.HttpResponse;
import cn.mzhong.kbus.http.HttpResponseHead;
import cn.mzhong.kbus.http.HttpResponseLine;

import java.io.InputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/31 14:08
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioResponse extends HttpResponse {

    protected final InputStream inputStream;

    public HttpBioResponse(HttpResponseHead head, InputStream inputStream) {
        super(head);
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
