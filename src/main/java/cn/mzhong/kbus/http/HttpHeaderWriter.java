package cn.mzhong.kbus.http;

import java.io.IOException;

public interface HttpHeaderWriter {

    void write(HttpRequest httpRequest, HttpUpstream httpUpstream) throws IOException;
}
