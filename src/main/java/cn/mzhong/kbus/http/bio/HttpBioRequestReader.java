package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpHeader;
import cn.mzhong.kbus.http.HttpLog;
import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpRequestLine;
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

    private HttpBioDownStream downStream;
    private int bufferSize;
    private HttpLog httpLog;
    private InputStream inputStream;

    HttpBioRequestReader(HttpBioDownStream downStream, int bufferSize) throws IOException {
        this.downStream = downStream;
        this.inputStream = downStream.getInputStream();
        this.bufferSize = bufferSize;
        this.httpLog = HttpLog.threadLocal.get();
    }

    public HttpRequest next() {
        if (downStream.isClosed()) {
            return null;
        }
        try {
            // 读第一行
            byte[] requestLineBytes = StreamUtils.readLine(inputStream);
            if (requestLineBytes == null) {
                return null;
            }
            this.httpLog.start();
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
            // 返回请求体
            return new HttpBioRequest(requestLine, header, inputStream);
        } catch (Exception e) {
            return null;
        } finally {
            this.httpLog.saveRequestExpand();
        }
    }
}
