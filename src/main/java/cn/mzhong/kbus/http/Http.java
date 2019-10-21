package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 15:19
 *
 * @author mzhong
 * @version 1.0
 */
public class Http {

    public final Set<Server> servers = new HashSet<>();
    final KBus bus;

    public Http(KBus parent) {
        this.bus = parent;
    }

    public void start() {
        for (Server server : servers) {
            server.start();
        }
    }

    public Http addServer(Server server) {
        this.servers.add(server);
        return this;
    }
}
