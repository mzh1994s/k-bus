package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.AbstractHttpAcceptor;
import cn.mzhong.kbus.http.Server;

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
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    try {
                        if (next.isAcceptable()) {
                            accept(next);
                        } else if (next.isReadable()) {
                            read(next);
                        } else if (next.isWritable()) {
                            write(next);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        iterator.remove();
                    }
                }

            }
        });
    }

    protected void accept(SelectionKey selectionKey) throws IOException {
        SocketChannel accept = this.socketChannel.accept();
        accept.configureBlocking(false);
        accept.register(this.selector, SelectionKey.OP_READ, new UpstreamHandler());
    }

    protected void read(SelectionKey selectionKey) throws IOException {
        UpstreamHandler upstreamHandler = (UpstreamHandler) selectionKey.attachment();
        upstreamHandler.handleDownStreamRead((SocketChannel) selectionKey.channel());
    }

    protected void write(SelectionKey selectionKey) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("182.151.197.163", 5000);
        SocketChannel upstreamChannel = SocketChannel.open(inetSocketAddress);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        upstreamChannel.read(byteBuffer);
    }
}
