package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.ArrayUtils;
import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 15:48
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioRequestReader {

    private Socket socket;
    private boolean isKeepAlive = false;
    private int bufferSize;
    private HttpLog httpLog;

    HttpBioRequestReader(Socket socket, int bufferSize) {
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.httpLog = HttpLog.threadLocal.get();
    }

    public HttpRequest next() {
        if (socket.isClosed()) {
            return null;
        }
        this.httpLog.start();
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // 读第一行
            byte[] firstLine = StreamUtils.readLine(inputStream);

            // 读header
            HttpRequestHeader httpHeader = new HttpRequestHeader();
            byte[][] headers = new byte[0][];
            byte[] lineBytes;
            while ((lineBytes = StreamUtils.readLine(inputStream)) != null) {
                String line = new String(lineBytes);
                if (line.startsWith("Host: ")) {
                    httpHeader.setHost(line.substring(6));
                    line = "Host: www.cqksy.cn";
                } else if (line.startsWith("Content-Length: ")) {
                    httpHeader.setContentLength(Integer.parseInt(line.substring(16)));
                } else if (line.startsWith("Connection: ")) {
                    isKeepAlive = "keep-alive".equals(line.substring(12));
                }
                // 空行判断
                if (line.isEmpty()) {
                    break;
                }
                headers = ArrayUtils.add(headers, lineBytes);
            }

            // 读内容
            byte[] content = StreamUtils.read(inputStream, bufferSize, httpHeader.getContentLength());

            return new HttpRequest(firstLine, headers, content, httpHeader, inputStream, outputStream);
        } catch (Exception e) {
            return null;
        } finally {
            if (!isKeepAlive) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
            this.httpLog.saveRequestExpand();
        }
    }
}
