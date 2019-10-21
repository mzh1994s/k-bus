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
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public final Http http = new Http(this);

    public void start() {
        http.start();
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }
}
