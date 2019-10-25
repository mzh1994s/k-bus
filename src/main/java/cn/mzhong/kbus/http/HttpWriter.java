package cn.mzhong.kbus.http;

import java.io.IOException;

public interface HttpWriter {

    void write(HttpRequest httpRequest, HttpUpstream httpUpstream, Location location) throws IOException;
}
