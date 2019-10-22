package cn.mzhong.kbus.core;

import java.io.IOException;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 9:36
 *
 * @author mzhong
 * @version 1.0
 */
public class SocketPool {

    public Socket getSocket() {
        return new Socket();
    }

    void returnSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {

        }
    }
}
