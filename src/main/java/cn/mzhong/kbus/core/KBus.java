package cn.mzhong.kbus.core;

import cn.mzhong.kbus.http.Http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 创建时间： 2019/10/18 17:25
 *
 * @author mzhong
 * @version 1.0
 */
public class KBus {

    private IOType ioType = IOType.BIO;
    /**
     * 临时使用这个，后面可能换创建实例的方式
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Http http = new Http(this);

    private SocketPool socketPool = new SocketPool();

    public ExecutorService getExecutor() {
        return executor;
    }

    public Http getHttp() {
        return http;
    }

    public SocketPool getSocketPool() {
        return socketPool;
    }

    public IOType getIoType() {
        return ioType;
    }

    public void setIoType(IOType ioType) {
        this.ioType = ioType;
    }

    public void start() {
        http.start();
    }

}
