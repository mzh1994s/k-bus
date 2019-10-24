package cn.mzhong.kbus.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private int num = 0;

    public synchronized Socket getSocket(String host, int port) throws InterruptedException, IOException {
        String key = host + ":" + port;
        BlockingQueue<Socket> pool = poolMap.get(key);
        if (pool == null) {
            pool = new LinkedBlockingQueue<>();
            poolMap.put(key, pool);
        }
        Socket socket;
        while (!pool.isEmpty()) {
            socket = pool.take();
            if (!socket.isClosed()) {
                return socket;
            }
            num--;
        }

        if (num < max) {
            socket = new Socket(host, port);
            num++;
            return socket;
        }
        socket = pool.take();
        if (socket.isClosed()) {
            num--;
            return getSocket(host, port);
        }
        return socket;
    }

    public synchronized void returnSocket(Socket socket) {
        InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        if (socketAddress != null && !socket.isClosed()) {
            try {
                poolMap.get(socketAddress.getHostName() + ":" + socketAddress.getPort()).put(socket);
            } catch (Exception ignored) {
            }
        } else {
            num--;
        }
    }
}
