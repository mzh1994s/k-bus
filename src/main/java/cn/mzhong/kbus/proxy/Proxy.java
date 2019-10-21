package cn.mzhong.kbus.proxy;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 16:30
 *
 * @author mzhong
 * @version 1.0
 */
public interface Proxy<A, C> {

    void proxy(A accept, C client);
}
