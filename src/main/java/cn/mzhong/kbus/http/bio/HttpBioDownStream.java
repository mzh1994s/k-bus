package cn.mzhong.kbus.http.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 11:00
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioDownStream {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public HttpBioDownStream(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
        try {
            outputStream.close();
        } catch (IOException ignored) {
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
