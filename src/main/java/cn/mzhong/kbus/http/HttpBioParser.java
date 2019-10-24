package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
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
public class HttpBioParser {

    private final static Logger log = LoggerFactory.getLogger(HttpBioParser.class);

    private ByteArrayOutputStream dataStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private boolean isKeepAlive = false;
    private int bufferSize;
    private HttpLog httpLog;

    HttpBioParser(Socket socket, int bufferSize) {
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
            this.dataStream = new ByteArrayOutputStream();
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            String[] split = this.doParseRequest();
            String method = split[0];
            String uri = split[1];
            String version = split[2];
            HttpHeader httpHeader = this.doParseHeader();
            this.doFinal(httpHeader);
            return new HttpRequest(method, uri, version, httpHeader,
                    dataStream.toByteArray(), inputStream, outputStream);
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

    private String[] doParseRequest() throws IOException {
        String firstLine = StreamUtils.readLineAndWrite(inputStream, dataStream);
        if (firstLine == null) {
            throw new IOException();
        }
        this.httpLog.setRequestLine(firstLine);
        return firstLine.split(" ");
    }

    private HttpHeader doParseHeader() throws IOException {
        HttpHeader httpHeader = new HttpHeader();
        String line;
        while ((line = StreamUtils.readLineAndWrite(inputStream, dataStream)) != null) {
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
        }
        return httpHeader;
    }

    private void doFinal(HttpHeader httpHeader) throws IOException {
        StreamUtils.copy(inputStream, outputStream, bufferSize, httpHeader.getContentLength());
    }
}
