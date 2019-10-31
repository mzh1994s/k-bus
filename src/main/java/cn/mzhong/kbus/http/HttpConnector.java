package cn.mzhong.kbus.http;

import cn.mzhong.kbus.http.bio.HttpBioDownStream;

import java.io.IOException;

/**
 * 上游连接器，在实现此接口时必须保证对象是线程安全的<br>
 * 创建时间： 2019/10/22 10:04
 *
 * @author mzhong
 * @version 1.0
 */
public interface HttpConnector {

    void connect(HttpBioDownStream downStream, HttpRequest request, Location location) throws IOException;
}
