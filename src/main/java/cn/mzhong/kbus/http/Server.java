package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.StreamUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

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
                try {
                    accept(server.accept());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Location locationMatch() {
        return null;
    }

    private void accept(Socket accept) {
        http.bus.getExecutorService().execute(() -> {
            try {
                acceptInternal(accept);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void acceptInternal(Socket accept) throws IOException, InterruptedException {
        InputStream acceptInputStream = null;
        OutputStream acceptOutputStream = null;
        InputStream socketInputStream = null;
        OutputStream socketOutputStream = null;
        Socket socket = null;
        try {
            // 在取header等信息时，需要读取accept的流，但是流只能获取一次，所以使用Byte流作为中转
            acceptInputStream = accept.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(acceptInputStream));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            String line;
            String[] headerNames = {"Host: ", "Connection: "};
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
            // 记得还有一个空行
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // 通过获取到的host名称匹配目标服务器
            socket = new Socket();
            socket.connect(new InetSocketAddress("www.cqksy.cn", 80));
            acceptOutputStream = accept.getOutputStream();
            socketInputStream = socket.getInputStream();
            socketOutputStream = socket.getOutputStream();
            // 将前面已经获取的数据先写进去
            socketOutputStream.write(byteArrayOutputStream.toByteArray());
            socketOutputStream.flush();
            // 将没有发送完的数据发送完成
            CountDownLatch sendLatch = StreamUtils.copyInThread(http.bus.getExecutorService(),
                    acceptInputStream, socketOutputStream);
            // 接收数据
            StreamUtils.copy(socketInputStream, acceptOutputStream);
            sendLatch.await();
        } finally {
            StreamUtils.closeSilent(acceptInputStream);
            StreamUtils.closeSilent(socketInputStream);
            StreamUtils.closeSilent(acceptOutputStream);
            StreamUtils.closeSilent(socketOutputStream);
            StreamUtils.closeSilent(accept);
            StreamUtils.closeSilent(socket);
        }
    }

    public Server addLocation(Location location) {
        this.locations.add(location);
        return this;
    }
}
