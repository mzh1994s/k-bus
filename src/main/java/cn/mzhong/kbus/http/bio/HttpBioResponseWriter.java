package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.HttpResponse;
import cn.mzhong.kbus.http.Location;
import cn.mzhong.kbus.http.bio.HttpBioDownStream;
import cn.mzhong.kbus.http.bio.HttpBioUpstream;

import java.io.IOException;

public interface HttpBioResponseWriter {

    void write(HttpBioUpstream upstream, HttpResponse response, HttpBioDownStream downStream, Location location) throws IOException;
}
