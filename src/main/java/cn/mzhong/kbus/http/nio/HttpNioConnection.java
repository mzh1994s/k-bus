package cn.mzhong.kbus.http.nio;

import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/11 9:08
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioConnection {
    private final SocketChannel channel;
    private final String key;

    public HttpNioConnection(SocketChannel channel, String host, int port) {
        this.channel = channel;
        this.key = host + ":" + port;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public String getKey() {
        return key;
    }
}
