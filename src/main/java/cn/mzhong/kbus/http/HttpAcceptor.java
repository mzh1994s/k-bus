package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.IOType;

import java.io.IOException;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 10:09
 *
 * @author mzhong
 * @version 1.0
 */
public interface HttpAcceptor {

    void start(Server server) throws IOException;

    static HttpAcceptor getInstance(IOType type) {
        if (type == IOType.NIO) {
            return new HttpBioAcceptor();
        } else {
            return new HttpBioAcceptor();
        }
    }
}
