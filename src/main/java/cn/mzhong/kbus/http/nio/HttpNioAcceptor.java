package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
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
    Selector downstreamSelector;
    Selector upstreamSelector;

    @Override
    protected void start() throws IOException {
        Server server = getServer();
        InetSocketAddress socketAddress = new InetSocketAddress(server.getListen());
        this.socketChannel = ServerSocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.bind(socketAddress);
        this.downstreamSelector = Selector.open();
        this.upstreamSelector = Selector.open();
        this.socketChannel.register(this.downstreamSelector, SelectionKey.OP_ACCEPT);
        this.downstreamListen();
        this.upstreamListen();
    }

    private Set<SelectionKey> await(Selector selector) {
        try {
            selector.select(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return selector.selectedKeys();
    }

    protected void downstreamListen() {
        getExecutor().execute(() -> {
            while (true) {
                Set<SelectionKey> selectionKeys = await(this.downstreamSelector);
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    iterator.remove();
                    try {
                        if (next.isAcceptable()) {
                            downstreamAccept(next);
                        } else if (next.isReadable()) {
                            downstreamRead(next);
                        } else if (next.isWritable()) {
                            downstreamWrite(next);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void upstreamListen() {
        getExecutor().execute(() -> {
            while (true) {
                Set<SelectionKey> selectionKeys = await(this.upstreamSelector);
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    iterator.remove();
                    try {
                        if (next.isReadable()) {
                            upstreamRead(next);
                        } else if (next.isWritable()) {
                            upstreamWrite(next);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            }
        });
    }

    protected void downstreamAccept(SelectionKey selectionKey) throws IOException {
        SocketChannel accept = this.socketChannel.accept();
        accept.configureBlocking(false);
        RequestContext context = new RequestContext();
        context.setDownstream(accept);
        accept.register(this.downstreamSelector, SelectionKey.OP_READ, context);
    }

    protected void downstreamRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        HttpHeadBuffer headBuffer = context.getHeadBuffer();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                headBuffer.add(buffer.get());
                if (headBuffer.isEof()) {
                    SocketChannel upstream = SocketChannel.open(new InetSocketAddress("182.151.197.163", 5000));
                    upstream.configureBlocking(false);
                    this.upstreamSelector.wakeup();
                    upstream.register(this.upstreamSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, context);
                    context.setUpstream(upstream);
                }
            }
        }
    }

    protected void downstreamWrite(SelectionKey selectionKey) throws IOException {
        SelectableChannel channel = selectionKey.channel();
        System.out.println("可写");
    }

    protected void upstreamRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        SocketChannel downStream = context.getDownstream();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while (channel.read(buffer) > 0) {
                buffer.flip();
                downStream.write(buffer);
                System.out.println(new String(buffer.array()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void upstreamWrite(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        HttpHeadBuffer headBuffer = context.getHeadBuffer();
        if (!context.isRequested() && headBuffer.isEof()) {
            byte[] bytes = headBuffer.toBytes();
            String headString = new String(bytes, StandardCharsets.ISO_8859_1);
            String[] split = headString.split("\r\n");
            HttpRequestLine requestLine = HttpRequestLine.parse(split[0]);
            HttpHeader header = new HttpHeader();
            int length = split.length;
            for (int i = 1; i < length; i++) {
                header.putLine(split[i]);
            }
            header.set("Host", "182.151.197.163:5000");
            HttpRequestHead httpHead = new HttpRequestHead(requestLine, header);
            HttpRequest request = new HttpRequest(httpHead);
            context.setRequest(request);
            System.out.println(new String(httpHead.toByteArray()));
            ByteBuffer requestBuffer = ByteBuffer.wrap(httpHead.toByteArray());
            channel.write(requestBuffer);
            context.setRequested(true);
        }
    }
}
