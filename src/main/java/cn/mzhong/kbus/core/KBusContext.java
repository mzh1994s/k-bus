package cn.mzhong.kbus.core;

import cn.mzhong.kbus.http.bio.HttpBioUpstreamPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO<br>
 * 创建时间： 2019/10/31 17:58
 *
 * @author mzhong
 * @version 1.0
 */
public class KBusContext {

    /**
     * 临时使用这个，后面可能换创建实例的方式
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ExecutorService getExecutor() {
        return executor;
    }

    private final HttpBioUpstreamPool bioUpstreamPool = new HttpBioUpstreamPool();

    public HttpBioUpstreamPool getBioUpstreamPool() {
        return bioUpstreamPool;
    }
}
