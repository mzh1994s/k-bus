package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.HttpRequest;
import cn.mzhong.kbus.http.HttpRequestHead;
import cn.mzhong.kbus.http.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * TODO<br>
 * 创建时间： 2019/11/13 14:31
 *
 * @author mzhong
 * @version 1.0
 */
public class DownstreamSelectorHandler extends SelectorHandler {

    private UpstreamSelectorHandler upstreamHandler;
    private ServerSocketChannel serverChannel;

    public DownstreamSelectorHandler(Server server) {
        super(server);
    }

    @Override
    public void start() {
        try {
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.configureBlocking(false);
            this.serverChannel.bind(new InetSocketAddress(server.getListen()));
            this.register(this.serverChannel, SelectionKey.OP_ACCEPT, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.start();
    }

    public void setUpstreamHandler(UpstreamSelectorHandler upstreamHandler) {
        this.upstreamHandler = upstreamHandler;
    }

    @Override
    void onAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
        SelectableChannel accept = channel.accept().configureBlocking(false);
        this.register(accept, SelectionKey.OP_READ, null);
    }

    private HttpNioContext getContext(SelectionKey downstreamKey) {
        SocketChannel channel = (SocketChannel) downstreamKey.channel();
        HttpNioContext context = (HttpNioContext) downstreamKey.attachment();
        if (context == null) {
            synchronized (downstreamKey) {
                context = (HttpNioContext) downstreamKey.attachment();
                if (context == null) {
                    context = new HttpNioContext();
                    context.setDownstreamKey(downstreamKey);
                    context.setDownstream(channel);
                    downstreamKey.attach(context);
                    System.out.println("创建连接：" + context.getDownstreamKey());
                }
            }
        }
        return context;
    }

    @Override
    void onRead(SelectionKey downstreamKey) throws IOException {
        HttpNioContext context = getContext(downstreamKey);
        SocketChannel downstream = context.getDownstream();
        HttpHeadReader headBuffer = context.getRequestHeadReader();
        ByteBuffer buffer = context.getInboundBuffer();
        buffer.clear();
        int read = downstream.read(buffer);
        if (read == -1) {
            throw new IOClosedException();
        }
        buffer.flip();
        if (headBuffer.isEof()) {
            // 上游启写
            SelectionKey upstreamKey = context.getUpstreamKey();
            upstreamKey.interestOps(upstreamKey.interestOps() | SelectionKey.OP_WRITE);
            this.upstreamHandler.wakeup();
        } else {
            // 读head
            while (buffer.hasRemaining()) {
                headBuffer.add(buffer.get());
                if (headBuffer.isEof()) {
                    SocketChannel upstream = SocketChannel.open(
                            new InetSocketAddress("localhost", 9000));
                    upstream.configureBlocking(false);
                    context.setUpstream(upstream);
                    HttpRequestHead httpHead = HttpRequestHead.parse(headBuffer);
                    HttpRequest request = new HttpRequest(httpHead);
                    context.setRequest(request);
                    context.setRequestWriter(new RequestWriter(context,
                            httpHead.getHeader().getIntValue("Content-Length")));
                    this.upstreamHandler.append(context);
                    break;
                }
            }
        }
    }

    private void keepWrite(SelectionKey downstreamKey) {
        downstreamKey.interestOps(downstreamKey.interestOps() | SelectionKey.OP_WRITE);
        this.wakeup();
    }

    @Override
    void onWrite(SelectionKey downstreamKey) throws IOException {
        HttpNioContext context = (HttpNioContext) downstreamKey.attachment();
        SelectionKey upstreamKey = context.getUpstreamKey();
        HttpWriter responseWriter = context.getResponseWriter();
        ByteBuffer buffer = context.getResponse().getHead().getBuffer();
        // 读取head时多读出来的，先送上
        if (buffer.hasRemaining()) {
            responseWriter.writeHead(buffer);
        }
        if (buffer.hasRemaining()) {
            this.keepWrite(downstreamKey);
            return;
        }
        buffer = context.getOutboundBuffer();
        if (responseWriter.writeBody(buffer) == IOStatus.EOF) {
            System.out.println("移除上下文：" + downstreamKey);
            upstreamKey.attach(null);
            downstreamKey.attach(null);
            upstreamKey.cancel();
            return;
        }
        if (buffer.hasRemaining()) {
            // 没写完，还要打开写事件
            this.keepWrite(downstreamKey);
        } else {
            upstreamKey.interestOps(upstreamKey.interestOps() | SelectionKey.OP_READ);
            upstreamHandler.wakeup();
        }
    }
}
