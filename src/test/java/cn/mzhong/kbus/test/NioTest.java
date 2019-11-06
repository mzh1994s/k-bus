package cn.mzhong.kbus.test;

import cn.mzhong.kbus.core.IOType;
import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.http.Location;
import cn.mzhong.kbus.http.Server;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;

import java.io.IOException;

/**
 * TODO<br>
 * 创建时间： 2019/11/6 14:50
 *
 * @author mzhong
 * @version 1.0
 */
public class NioTest {
    public static void main(String[] args) throws IOException {
        KBus bus = new KBus();
        bus.setBufferSize(8192);

        Server server2 = bus.getHttp().createServer();
        server2.setListen(9001);
        server2.setIo(IOType.NIO);
        Location location2 = server2.createLocation();
        location2.setValue("/");
        location2.setProxyPass("http://182.151.197.163:5000");
        location2.setChunkedTransferEncoding(ChunkedTransferEncoding.OFF);

        bus.start();
    }
}
