package cn.mzhong.kbus.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 9:36
 *
 * @author mzhong
 * @version 1.0
 */
public class SocketPool {

    Map<SocketAddress, BlockingQueue<Socket>> poolMap = new HashMap<>();

    private int max = 30;
    private int num = 0;

    public synchronized Socket getSocket(String host, int port) throws InterruptedException, IOException {
        SocketAddress address = new InetSocketAddress(host, port);
        BlockingQueue<Socket> pool = poolMap.get(address);
        if (pool == null) {
            pool = new LinkedBlockingDeque<>();
            poolMap.put(address, pool);
        }
        if (pool.isEmpty()) {
            if (num < max) {
                Socket socket = new Socket();
                socket.connect(address);
                pool.put(socket);
                num++;
            }
        }
        Socket take;
        while ((take = pool.take()).isClosed()) ;
        return take.isClosed() ? getSocket(host, port) : take;
    }

    public void returnSocket(Socket socket) throws InterruptedException {
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        if (socketAddress != null && !socket.isClosed()) {
            poolMap.get(socketAddress).put(socket);
        }
    }
}
