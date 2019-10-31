package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:03
 *
 * @author mzhong
 * @version 1.0
 */
public abstract class AbstractHttpAcceptor implements HttpAcceptor {

    private KBus bus;
    private Http http;
    private Server server;
    private ExecutorService executor;

    @Override
    public void start(Server server) throws IOException {
        this.http = server.getHttp();
        this.bus = this.http.getBus();
        this.server = server;
        this.executor = this.bus.getExecutor();
        this.start();
    }

    protected abstract void start() throws IOException;

    public KBus getBus() {
        return bus;
    }

    public Http getHttp() {
        return http;
    }

    public Server getServer() {
        return server;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
