package cn.mzhong.kbus.http.nio;

/**
 * 类似于netty那样，只不过这里是将handler作为链表的形式。上下文只有一个<br>
 * 创建时间： 2019/11/12 9:24
 *
 * @param <T> 实现类的类型
 * @author mzhong
 * @version 1.0
 */
public abstract class NioHandler<T> {

    /**
     * 前一个处理器
     */
    private T prev;
    /**
     * 后一个处理器
     */
    private T next;

    public T getPrev() {
        return prev;
    }

    public void setPrev(T prev) {
        this.prev = prev;
    }

    public T getNext() {
        return next;
    }

    public void setNext(T next) {
        this.next = next;
    }

    abstract void onRead(RequestContext context);

    abstract void onWrite(RequestContext context);
}
