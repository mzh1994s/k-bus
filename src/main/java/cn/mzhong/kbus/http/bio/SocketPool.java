package cn.mzhong.kbus.http.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 9:36
 *
 * @author mzhong
 * @version 1.0
 */
public class SocketPool {

    Map<String, BlockingQueue<Socket>> poolMap = new HashMap<>();

    private int max = 30;
    private AtomicInteger num = new AtomicInteger();

    public Socket get(String host, int port) throws IOException {
        String key = host + ":" + port;
        BlockingQueue<Socket> pool = poolMap.get(key);
        if (pool == null) {
            pool = new LinkedBlockingQueue<>();
            poolMap.put(key, pool);
        }
        Socket socket;
        while (!pool.isEmpty()) {
            try {
                socket = pool.take();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (!socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                return socket;
            }
            num.getAndDecrement();
        }

        if (num.get() < max) {
            long start = System.currentTimeMillis();
            socket = new Socket(host, port);
            System.out.println("创建用时：" + (System.currentTimeMillis() - start));
            num.getAndIncrement();
            return socket;
        }
        try {
            socket = pool.take();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (socket.isClosed()) {
            num.getAndDecrement();
            return get(host, port);
        }
        return socket;
    }

    public void back(Socket socket) {
        long start = System.currentTimeMillis();
        InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        if (socketAddress != null && !socket.isClosed()) {
            try {
                poolMap.get(socketAddress.getHostName() + ":" + socketAddress.getPort()).put(socket);
            } catch (Exception ignored) {
            }
        } else {
            num.getAndDecrement();
        }
        System.out.println("归还用时：" + (System.currentTimeMillis() - start));
    }
}
