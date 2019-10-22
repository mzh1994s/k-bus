package cn.mzhong.kbus.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    private ByteArrayOutputStream tempStream;
    private InputStream inputStream;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter tempWriter;

    HttpBioParser(Socket socket) {
        this.socket = socket;
    }

    public HttpRequest next() {
        try {
            this.tempStream = new ByteArrayOutputStream();
            this.inputStream = socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            this.tempWriter = new BufferedWriter(new OutputStreamWriter(tempStream));
            long start = System.currentTimeMillis();
            String[] split = this.doParseRequest();
            HttpHeader httpHeader = this.doParseHeader();
            this.doFinal(httpHeader);
            if (log.isDebugEnabled()) {
                log.debug("解析请求耗时：" + (System.currentTimeMillis() - start));
            }
            return new HttpRequest(split[0], split[1], split[2], httpHeader,
                    tempStream.toByteArray(), inputStream, socket.getOutputStream());
        } catch (Exception e) {
            return null;
        }
    }

    private String[] doParseRequest() throws IOException {
        String firstLine = reader.readLine();
        if (firstLine == null) {
            throw new IOException();
        }
        tempWriter.write(firstLine);
        tempWriter.newLine();
        return firstLine.split(" ");
    }

    private HttpHeader doParseHeader() throws IOException {
        HttpHeader httpHeader = new HttpHeader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Host: ")) {
                httpHeader.setHost(line.substring(6));
                line = "Host: www.cqksy.cn";
            } else if (line.startsWith("Content-Length: ")) {
                httpHeader.setContentLength(Integer.parseInt(line.substring(16)));
            } else if (line.startsWith("Connection: ")) {
                continue;
            }
            tempWriter.write(line);
            tempWriter.newLine();
            if ("".equals(line)) {
                break;
            }
        }
        return httpHeader;
    }

    private void doFinal(HttpHeader httpHeader) throws IOException {
        int contentLength = httpHeader.getContentLength();
        if (contentLength != 0) {
            char[] buf = new char[contentLength];
            int read = this.reader.read(buf);
            if (read != -1) {
                this.tempWriter.write(buf);
            }
        }
        this.tempWriter.flush();
    }
}
