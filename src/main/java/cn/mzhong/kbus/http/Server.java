package cn.mzhong.kbus.http;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 15:19
 *
 * @author mzhong
 * @version 1.0
 */
public class Server {
    /**
     * 监听的端口
     */
    private int listen;
    /**
     * 绑定的域名
     */
    private String serverName;
    /**
     * 等同于nginx的Location
     */
    private Set<Location> locations = new HashSet<>();

    private Http http;

    private ServerSocket server;

    public Server(Http http) {
        this.http = http;
    }

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    private void initSocket() {
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(listen));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        initSocket();
        http.bus.getExecutorService().execute(() -> {
            while (true) {
                http.bus.getExecutorService().execute(() -> {
                    try {
                        accept(server.accept());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private Location locationMatch() {
        return null;
    }

    private void accept(Socket accept) throws IOException {
        // 在取header等信息时，需要读取accept的流，但是流只能获取一次，所以使用Byte流作为中转
        InputStream acceptInputStream = accept.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
        // 第一行没有header信息。
        String line = bufferedReader.readLine();
        bufferedWriter.write(line);
        bufferedWriter.newLine();
        String[] headerNames = {"Host: "};
        // 请求报文会有一个空行，读到空行就结束
        while (!"".equals(line = bufferedReader.readLine())) {
            for (String headerName : headerNames) {
                if (line.startsWith(headerName)) {
                    System.out.println(line.substring(headerName.length()));
                }
            }
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
        // client
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8080));
        OutputStream acceptOutputStream = accept.getOutputStream();
        InputStream socketInputStream = socket.getInputStream();
        OutputStream socketOutputStream = socket.getOutputStream();
        sendAndReceive(acceptInputStream, acceptOutputStream, socketInputStream, socketOutputStream);
    }

    private static void sendAndReceive(
            InputStream acceptInputStream, OutputStream acceptOutputStream,
            InputStream socketInputStream, OutputStream socketOutputStream) {
        try {
            copy(acceptInputStream, socketOutputStream);
            copy(socketInputStream, acceptOutputStream);
            wait(acceptInputStream);
            wait(socketInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(acceptInputStream);
            close(socketInputStream);
            close(acceptOutputStream);
            close(socketOutputStream);
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        new Thread(() -> {
            try {
                byte[] buf = new byte[4096];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                    outputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                notify(inputStream);
            }
        }).start();
    }

    private static void wait(Object object) throws InterruptedException {
        synchronized (object) {
            object.wait();
        }
    }

    private static void notify(Object object) {
        synchronized (object) {
            object.notify();
        }
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server addLocation(Location location) {
        this.locations.add(location);
        return this;
    }
}
