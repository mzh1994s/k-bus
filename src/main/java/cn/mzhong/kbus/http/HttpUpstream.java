package cn.mzhong.kbus.http;

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
public class HttpUpstream {

    private InputStream inputStream;

    private OutputStream outputStream;

    public HttpUpstream(Socket socket) throws IOException {
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
