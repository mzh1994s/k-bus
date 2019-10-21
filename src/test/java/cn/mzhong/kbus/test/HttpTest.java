package cn.mzhong.kbus.test;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.http.Location;
import cn.mzhong.kbus.http.Server;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 16:56
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpTest {
    public static void main(String[] args) {
        KBus bus = new KBus();
        Server server = new Server(bus.http);
        server.setListen(9001);
        Location location = new Location();
        location.setValue("/");
        location.setProxyPass("http://www.neea.edu.cn/");
        server.addLocation(location);
        bus.http.servers.add(server);
        bus.start();
    }
}
