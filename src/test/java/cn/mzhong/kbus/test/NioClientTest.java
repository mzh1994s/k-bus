package cn.mzhong.kbus.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 8:36
 *
 * @author mzhong
 * @version 1.0
 */
public class NioClientTest {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("182.151.197.163", 5000));
    }
}
