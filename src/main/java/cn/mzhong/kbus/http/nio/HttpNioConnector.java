package cn.mzhong.kbus.http.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 9:08
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioConnector {

    Selector selector;

    public HttpNioConnector() throws IOException {
        selector = Selector.open();
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    selector.select();
                } catch (IOException ignored) {
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        try {
                            this.read(selectionKey);
                        } catch (IOException ignored) {
                        }
                    }
                    if (selectionKey.isWritable()) {
                        try {
                            this.write(selectionKey);
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }).start();
    }

    SocketChannel connect(ChannelHandler chanelHandler) throws IOException {
        SocketChannel upstream = SocketChannel.open(new InetSocketAddress("182.151.197.163", 5000));
        chanelHandler.setUpstream(upstream);
        upstream.configureBlocking(false);
        selector.wakeup();
        upstream.register(selector, SelectionKey.OP_READ, chanelHandler);
        return upstream;
    }

    void write(SelectionKey selectionKey) throws IOException {
        ChannelHandler channelHandler = (ChannelHandler) selectionKey.attachment();
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        HttpHeadBuffer httpHeadBuffer = channelHandler.getHttpHeadBuffer();
        byte[] bytes = httpHeadBuffer.toBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        channel.write(byteBuffer);
        channel.register(selector, SelectionKey.OP_READ);
    }

    void read(SelectionKey selectionKey) throws IOException {
        ChannelHandler channelHandler = (ChannelHandler) selectionKey.attachment();
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        SocketChannel downstream = channelHandler.getDownstream();
        ByteBuffer byteBuffer = channelHandler.getBuffer();
        System.out.println(new String(byteBuffer.array()));
        while (channel.read(byteBuffer) > 0) {
            downstream.write(byteBuffer);
        }
    }
}
