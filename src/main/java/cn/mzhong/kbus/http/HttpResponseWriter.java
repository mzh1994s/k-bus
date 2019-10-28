package cn.mzhong.kbus.http;

import java.io.IOException;

public interface HttpResponseWriter {

    void write(HttpUpstream upstream, HttpResponse response, HttpDownStream downStream, Location location) throws IOException;
}
