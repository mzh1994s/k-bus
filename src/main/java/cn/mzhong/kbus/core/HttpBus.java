package cn.mzhong.kbus.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 创建时间： 2019/10/18 17:25
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBus {

    public static void connect() throws IOException {
        // server
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(8443));

        new Thread(() -> {
            while (true) {
                try {
                    Socket accept = server.accept();
                    // client
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("www.baidu.com", 443));
                    copy(accept.getInputStream(), socket.getOutputStream());
                    copy(socket.getInputStream(), accept.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        new Thread(() -> {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                    stringBuilder.append(new String(buf, 0, len));
                }
                System.out.println("复制完成：" + stringBuilder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        connect();
        System.out.println("hello");
    }
}
