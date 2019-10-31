package cn.mzhong.kbus.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/10/29 9:31
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioAcceptor extends AbstractHttpAcceptor {

    ServerSocketChannel socketChannel;
    Selector selector;

    @Override
    protected void start() throws IOException {
        this.socketChannel = ServerSocketChannel.open();
        Server server = getServer();
        InetSocketAddress socketAddress = new InetSocketAddress(server.getListen());
        // 非阻塞
        this.socketChannel.configureBlocking(false);
        this.socketChannel.bind(socketAddress);
        this.selector = Selector.open();
        this.socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        this.listen();
    }

    protected void listen() {
        getExecutor().execute(() -> {
            Selector selector = HttpNioAcceptor.this.selector;
            while (true) {
                try {
                    selector.select();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                try {
                    while (iterator.hasNext()) {
                        SelectionKey next = iterator.next();
                        if (next.isAcceptable()) {
                            accept(next);
                        } else if (next.isReadable()) {
                            read(next);
                        }
                        iterator.remove();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void accept(SelectionKey selectionKey) throws IOException {
        SocketChannel accept = this.socketChannel.accept();
        accept.configureBlocking(false);
        accept.register(this.selector, SelectionKey.OP_READ);
        selectionKey.attach(new ByteArrayOutputStream());
    }

    protected void read(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        int read = channel.read(byteBuffer);
        selectionKey.attachment();
        if (read == -1) {
            channel.close();
        } else {
            System.out.println("输出" + new String(byteBuffer.array()));
        }
    }
}
