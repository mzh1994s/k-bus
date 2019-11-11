package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TODO<br>
 * 创建时间： 2019/11/8 9:07
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioConnectionFactory {

    private final Map<String, Queue<SocketChannel>> channelPoolMap = new ConcurrentHashMap<>();

    /**
     * 根据host : port 获取连接池，如果连接池不存在则会创建
     *
     * @param host
     * @param port
     * @return
     */
    private Queue<SocketChannel> getChannelPool(String host, int port) {
        String key = host + ":" + port;
        Queue<SocketChannel> channelPool = channelPoolMap.get(key);
        if (channelPool == null) {
            synchronized (channelPoolMap) {
                channelPool = channelPoolMap.get(key);
                if (channelPool == null) {
                    channelPool = new ConcurrentLinkedQueue<>();
                    channelPoolMap.put(key, channelPool);
                }
            }
        }
        return channelPool;
    }

    SocketChannel getConnection(String host, int port) throws IOException {
        Queue<SocketChannel> channelPool = getChannelPool(host, port);
        SocketChannel channel = channelPool.poll();
        if (channel == null) {
            channel = SocketChannel.open(new InetSocketAddress(host, port));
        }
        channel.configureBlocking(false);
        return channel;
    }
}
