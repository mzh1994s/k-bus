package cn.mzhong.kbus.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/31 16:28
 *
 * @author mzhong
 * @version 1.0
 */
public class SocketTest {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1024);
        Socket socket = new Socket("localhost", 1024);
        serverSocket.close();
        System.out.println(socket.isConnected());
        System.out.println(socket.isClosed());
        System.out.println(socket.isInputShutdown());
    }
}
