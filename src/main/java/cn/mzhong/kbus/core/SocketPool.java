package cn.mzhong.kbus.core;

import java.net.Socket;
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

    BlockingQueue<Socket> pool = new LinkedBlockingDeque<>();

    private int max = 30;
    private int num = 0;

    public Socket getSocket() throws InterruptedException {
        if (pool.isEmpty()) {
            if (num < max) {
                Socket socket = new Socket();
                pool.put(socket);
                num++;
            }
        }
        Socket take;
        while ((take = pool.take()).isClosed()) ;
        return take.isClosed() ? getSocket() : take;
    }

    public void returnSocket(Socket socket) throws InterruptedException {
        pool.put(socket);
    }
}
