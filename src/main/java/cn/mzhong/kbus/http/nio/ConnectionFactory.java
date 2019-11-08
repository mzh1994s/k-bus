package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 9:07
 *
 * @author mzhong
 * @version 1.0
 */
public class ConnectionFactory {

    private SocketChannel channel;

    ConnectionFactory() {
        try {
            channel = SocketChannel.open(new InetSocketAddress("182.151.197.163", 5000));
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SocketChannel getConnection(String host, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        return socketChannel;
    }
}
