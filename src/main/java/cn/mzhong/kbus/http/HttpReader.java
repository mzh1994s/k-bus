package cn.mzhong.kbus.http;

public interface HttpReader<T> {

    T read(HttpRequest request, HttpUpstream upstream, Location location);
}
