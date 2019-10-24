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
    /**
     * 临时使用这个，后面可能换创建实例的方式
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Http http = new Http(this);

    private Config config;

    private SocketPool socketPool = new SocketPool();

    public KBus() {
    }

    public KBus(Config config) {
        this.config = config;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Http getHttp() {
        return http;
    }

    public SocketPool getSocketPool() {
        return socketPool;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void start() {
        if (this.config == null) {
            this.config = new Config();
        }
        http.start();
    }

    public static class Config {
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
    }
}
