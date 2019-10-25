package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/23 16:16
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpConstant {

    public final static int CONF_OFF = 0;
    public final static int CONF_ON = 1;

    public final static String HEADER_PREFIX_TRANSFER_ENCODING = "Transfer-Encoding: ";
    public final static String HEADER_PREFIX_CONTENT_LENGTH = "Content-Length: ";
    public final static String HEADER_PREFIX_CONNECTION = "Connection: ";


    public final static byte[] LINE_SEPARATOR = {'\r', '\n'};

    private HttpConstant() {
    }

}
