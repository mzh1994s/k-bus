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
public class KBus implements Reloadable, Startable {
    public final static String VERSION = "1.0";

    /*----------------------基准配置 start*/
    private int bufferSize = 4096;
    private IOType io = IOType.BIO;

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public IOType getIo() {
        return io;
    }

    public void setIo(IOType io) {
        this.io = io;
    }
    /*----------------------基准配置 end*/

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

    @Override
    public void start() {
        http.start();
    }

    @Override
    public void reload() {
        http.reload();
    }
}
