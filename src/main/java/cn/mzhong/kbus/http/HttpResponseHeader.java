package cn.mzhong.kbus.http;

import cn.mzhong.kbus.http.header.Connection;
import cn.mzhong.kbus.http.header.TransferEncoding;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:52
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpResponseHeader {

    private int contentLength;
    private Connection connection;
    private TransferEncoding[] transferEncodings;

    public int getContentLength() {
        return contentLength;
    }

    public Connection getConnection() {
        return connection;
    }

    public TransferEncoding[] getTransferEncodings() {
        return transferEncodings;
    }

    public void add(byte[] lineBytes) {
        String line = new String(lineBytes);
        if (line.startsWith(HttpConstant.HEADER_PREFIX_CONTENT_LENGTH)) {
            this.contentLength = Integer.parseInt(line.substring(HttpConstant.HEADER_PREFIX_CONTENT_LENGTH.length()));
        } else if (line.startsWith(HttpConstant.HEADER_PREFIX_CONNECTION)) {
            this.connection = Connection.valueOfString(line.substring(HttpConstant.HEADER_PREFIX_CONNECTION.length()));
        }
    }
}
