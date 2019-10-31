package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.Location;

import java.io.IOException;

public interface HttpBioRequestWriter {

    void write(HttpBioRequest httpRequest, HttpBioUpstream httpUpstream, Location location) throws IOException;
}
