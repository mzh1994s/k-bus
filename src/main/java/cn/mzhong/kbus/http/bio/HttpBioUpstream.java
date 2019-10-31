package cn.mzhong.kbus.http.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:05
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioUpstream {

    private final String host;
    private final int port;

    private final Socket socket;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    public HttpBioUpstream(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isClosed() {
        return this.socket.isClosed();
    }

    public void close() {
        try {
            this.socket.close();
        } catch (IOException ignored) {
        }
    }
}
