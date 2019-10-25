package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;

/**
 * 用于自定义错误消息，页面等<br>
 * 创建时间： 2019/10/25 9:35
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpMessage {
    /**
     * 服务器内部异常（当代理服务器自身异常时）
     */
    public final static String HTTP_MESSAGE_500 = "HTTP/1.1 500 Internal Server Error\r\n" +
            "Connection: close\n\n" +
            "Content-Type: text/html\n\n" +
            "Server: KBUS/" + KBus.VERSION + "\r\n\r\n";

    /**
     * 从上游获取数据异常时（上游服务器发生的错误）
     */
    public final static String HTTP_MESSAGE_502 = "HTTP/1.1 502 Internal Server Error\r\n" +
            "Connection: close\n\n" +
            "Content-Type: text/html\n\n" +
            "Server: KBUS/" + KBus.VERSION + "\r\n\r\n";

    private String message500;

    private String message502;

    public String getMessage500() {
        return message500 == null ? HTTP_MESSAGE_500 : message500;
    }

    public void setMessage500(String message500) {
        this.message500 = message500;
    }

    public String getMessage502() {
        return message502 == null ? HTTP_MESSAGE_502 : message502;
    }

    public void setMessage502(String message502) {
        this.message502 = message502;
    }
}
