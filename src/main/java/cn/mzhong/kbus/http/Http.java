package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.Reloadable;
import cn.mzhong.kbus.core.Startable;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;

import java.util.HashSet;
import java.util.Set;

/**
 * Http模块<br>
 * 创建时间： 2019/10/21 15:19
 *
 * @author mzhong
 * @version 1.0
 */
public class Http implements Startable, Reloadable {

    private final Set<Server> servers = new HashSet<>();
    private final KBus bus;

    /**
     * 实现nginx中的chunked_transfer_encoding 配置项。
     */
    private ChunkedTransferEncoding chunkedTransferEncoding = ChunkedTransferEncoding.ON;

    public ChunkedTransferEncoding getChunkedTransferEncoding() {
        return chunkedTransferEncoding;
    }

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

    @Override
    public void reload() {

    }
}
