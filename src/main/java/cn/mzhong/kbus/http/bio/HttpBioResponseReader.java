package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.*;
import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 9:59
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioResponseReader {

    public HttpResponse read(InputStream inputStream) throws IOException {
        // 读取报文头
        byte[] responseLineBytes = StreamUtils.readLine(inputStream);
        if (responseLineBytes == null) {
            throw new IOEOFException();
        }
        HttpResponseLine responseLine = HttpResponseLine.parse(responseLineBytes);
        // 读header
        HttpHeader header = new HttpHeader();
        byte[] lineBytes;
        while ((lineBytes = StreamUtils.readLine(inputStream)) != null) {
            if (lineBytes.length == 0) {
                break;
            }
            header.putLine(lineBytes);
        }
        return new HttpResponse(new HttpResponseHead(responseLine, header));
    }
}
