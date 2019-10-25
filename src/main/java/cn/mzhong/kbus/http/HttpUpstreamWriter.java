package cn.mzhong.kbus.http;

import java.io.InputStream;
import java.io.OutputStream;

public interface HttpUpstreamWriter {

    void write(InputStream inputStream, OutputStream outputStream);
}
