package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 10:05
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpRequestHeader {

    private String host;

    private int contentLength;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
}
