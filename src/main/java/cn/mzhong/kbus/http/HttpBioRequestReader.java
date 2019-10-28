package cn.mzhong.kbus.http;

import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 15:48
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioRequestReader {

    private HttpDownStream downStream;
    private int bufferSize;
    private HttpLog httpLog;
    private InputStream inputStream;

    HttpBioRequestReader(HttpDownStream downStream, int bufferSize) throws IOException {
        this.downStream = downStream;
        this.inputStream = downStream.getInputStream();
        this.bufferSize = bufferSize;
        this.httpLog = HttpLog.threadLocal.get();
    }

    public HttpRequest next() {
        if (downStream.isClosed()) {
            return null;
        }
        this.httpLog.start();
        try {
            // 读第一行
            byte[] requestLineBytes = StreamUtils.readLine(inputStream);
            if (requestLineBytes == null) {
                return null;
            }
            HttpRequestLine requestLine = HttpRequestLine.parse(requestLineBytes);
            // 读header
            HttpHeader header = new HttpHeader();
            byte[] lineBytes;
            while ((lineBytes = StreamUtils.readLine(inputStream)) != null) {
                // 空行判断
                if (lineBytes.length == 0) {
                    break;
                }
                header.add(lineBytes);
            }
            // 读内容
            int contentLength = header.getIntValue(HttpHeader.CONTENT_LENGTH);
            byte[] content = StreamUtils.read(inputStream, contentLength);
            // 返回请求体
            return new HttpRequest(requestLine, header, content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.httpLog.saveRequestExpand();
        }
    }
}
