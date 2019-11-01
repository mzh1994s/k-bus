package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.KBusContext;

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

    protected KBus bus;
    protected Http http;
    protected Server server;
    protected ExecutorService executor;
    protected KBusContext beanFactory;

    @Override
    public void start(Server server) throws IOException {
        this.http = server.getHttp();
        this.bus = this.http.getBus();
        this.server = server;
        this.beanFactory = this.bus.getContext();
        this.executor = this.beanFactory.getExecutor();
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

    public KBusContext getBeanFactory() {
        return beanFactory;
    }
}
