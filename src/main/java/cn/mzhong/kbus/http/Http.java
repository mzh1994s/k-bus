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

    private final Set<Server> servers = new HashSet<>();
    private final KBus bus;

    public Http(KBus parent) {
        this.bus = parent;
    }

    public KBus getBus() {
        return bus;
    }

    public Server createServer() {
        Server server = new Server(this);
        servers.add(server);
        return server;
    }

    public void start() {
        for (Server server : servers) {
            server.start();
        }
    }
}
