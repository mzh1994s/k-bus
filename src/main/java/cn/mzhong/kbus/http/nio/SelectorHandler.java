package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.core.Reloadable;
import cn.mzhong.kbus.core.Startable;
import cn.mzhong.kbus.http.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * TODO<br>
 * 创建时间： 2019/11/13 14:10
 *
 * @author mzhong
 * @version 1.0
 */
public abstract class SelectorHandler implements Startable, Reloadable {

    private static final Logger log = LoggerFactory.getLogger(SelectorHandler.class);
    protected final ExecutorService executor;
    protected final Server server;
    private Selector selector;

    public SelectorHandler(Server server) {
        this.server = server;
        this.executor = server.getHttp().getBus().getContext().getExecutor();
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void start() {
        this.executor.execute(() -> {
            while (true) {
                try {
                    this.select();
                } catch (IOException e) {
                    log.debug("select故障", e);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                }
            }
        });
    }

    private void handleConnect(SelectionKey selectionKey) {
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_CONNECT);
        this.onConnect(selectionKey);
    }

    private void handleRead(SelectionKey selectionKey) {
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
        this.executor.execute(() -> {
            try {
                this.onRead(selectionKey);
            } catch (IOException e) {
                selectionKey.cancel();
            }
        });
    }

    private void handleWrite(SelectionKey selectionKey) {
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        this.executor.execute(() -> {
            try {
                this.onWrite(selectionKey);
            } catch (IOException e) {
                selectionKey.cancel();
            }
        });
    }

    protected void select() throws IOException {
        this.selector.select();
        Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            iterator.remove();
            if (selectionKey.isAcceptable()) {
                this.onAccept(selectionKey);
            } else if (selectionKey.isConnectable()) {
                this.handleConnect(selectionKey);
            } else if (selectionKey.isReadable()) {
                this.handleRead(selectionKey);
            } else if (selectionKey.isWritable()) {
                this.handleWrite(selectionKey);
            }
        }
        this.onSelected(selectionKeys);
    }

    /**
     * 当可接受时调用
     *
     * @param selectionKey
     */
    void onAccept(SelectionKey selectionKey) throws IOException {
    }

    /**
     * 当连接事件发生时调用
     *
     * @param selectionKey
     */
    void onConnect(SelectionKey selectionKey) {
    }

    /**
     * 当可读时调用
     *
     * @param selectionKey
     */
    void onRead(SelectionKey selectionKey) throws IOException {
    }

    /**
     * 当可写时调用
     *
     * @param selectionKey
     */
    void onWrite(SelectionKey selectionKey) throws IOException {
    }

    /**
     * 当select完成后调用
     *
     * @param selectionKeys
     */
    void onSelected(Set<SelectionKey> selectionKeys) {
    }

    /**
     * 将通道注册到选择器中
     *
     * @param channel
     * @param interestOps
     * @param attach
     * @throws ClosedChannelException
     */
    SelectionKey register(SelectableChannel channel, int interestOps, Object attach) throws ClosedChannelException {
        return channel.register(this.selector, interestOps, attach);
    }

    /**
     * 唤醒阻塞
     */
    void wakeup() {
        this.selector.wakeup();
    }
}
