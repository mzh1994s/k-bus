package cn.mzhong.kbus.http.bio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO<br>
 * 创建时间： 2019/10/31 17:19
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioUpstreamPool {

    Map<String, BlockingQueue<HttpBioUpstream>> poolMap = new HashMap<>();

    private int max = 30;
    private AtomicInteger num = new AtomicInteger();

    public HttpBioUpstream get(String host, int port) throws IOException {
        String key = key(host, port);
        BlockingQueue<HttpBioUpstream> pool = poolMap.get(key);
        if (pool == null) {
            pool = new LinkedBlockingQueue<>();
            poolMap.put(key, pool);
        }
        HttpBioUpstream bioUpstream;
        while (!pool.isEmpty()) {
            try {
                bioUpstream = pool.take();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (!bioUpstream.isClosed()) {
                return bioUpstream;
            }
            num.getAndDecrement();
        }

        if (num.get() < max) {
            bioUpstream = new HttpBioUpstream(host, port);
            num.getAndIncrement();
            return bioUpstream;
        }
        try {
            bioUpstream = pool.take();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (bioUpstream.isClosed()) {
            num.getAndDecrement();
            return get(host, port);
        }
        return bioUpstream;
    }

    public void back(HttpBioUpstream upstream) {
        if (!upstream.isClosed()) {
            try {
                poolMap.get(key(upstream.getHost(), upstream.getPort())).put(upstream);
            } catch (Exception ignored) {
            }
        } else {
            num.getAndDecrement();
        }
    }

    private String key(String host, int port) {
        return host + ":" + port;
    }
}
