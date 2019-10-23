package cn.mzhong.kbus.http;

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
public class HttpBioParser2 {

    private final static Logger log = LoggerFactory.getLogger(HttpBioParser2.class);

    private ByteArrayOutputStream dataStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;

    HttpBioParser2(Socket socket) {
        this.socket = socket;
    }

    public HttpRequest next() {
        try {
            this.dataStream = new ByteArrayOutputStream();
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            long start = System.currentTimeMillis();
            String[] split = this.doParseRequest();
            HttpHeader httpHeader = this.doParseHeader();
            this.doFinal(httpHeader);
            if (log.isDebugEnabled()) {
                log.debug("解析请求耗时：" + (System.currentTimeMillis() - start));
            }
            return new HttpRequest(split[0], split[1], split[2], httpHeader,
                    dataStream.toByteArray(), inputStream, outputStream);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] doParseRequest() throws IOException {
        String firstLine = readLineAndWriteDataStream();
        if (firstLine == null) {
            throw new IOException();
        }
        return firstLine.split(" ");
    }

    private HttpHeader doParseHeader() throws IOException {
        HttpHeader httpHeader = new HttpHeader();
        String line;
        while ((line = readLineAndWriteDataStream()) != null) {
            if (line.startsWith("Host: ")) {
                httpHeader.setHost(line.substring(6));
                line = "Host: www.cqksy.cn";
            } else if (line.startsWith("Content-Length: ")) {
                httpHeader.setContentLength(Integer.parseInt(line.substring(16)));
            }
            if ("".equals(line)) {
                break;
            }
        }
        return httpHeader;
    }

    private void doFinal(HttpHeader httpHeader) throws IOException {
        int contentLength = httpHeader.getContentLength();
        if (contentLength == 0) {
            return;
        }
        int read;
        for (int len = 0; len < contentLength; len++) {
            read = inputStream.read();
            if (read == -1) {
                break;
            }
            dataStream.write(read);
        }
    }

    /**
     * 读取一行，并且将它写入dataStream
     *
     * @return
     * @throws IOException
     */
    private String readLineAndWriteDataStream() throws IOException {

        // 读取为null的情况
        int read = inputStream.read();
        int previous = read;
        if (read == -1) {
            return null;
        }
        dataStream.write(read);
        ByteArrayOutputStream lineStream = new ByteArrayOutputStream();
        // 写行流，不写换行符
        if (read != '\r' && read != '\n') {
            lineStream.write(read);
        }

        while ((read = inputStream.read()) != -1) {
            // 写数据流
            dataStream.write(read);
            // 写行流，不写换行符
            if (read != '\r' && read != '\n') {
                lineStream.write(read);
            }
            // 一行
            if (previous == '\r' && read == '\n') {
                break;
            }
            previous = read;
        }
        return new String(lineStream.toByteArray());
    }

}
