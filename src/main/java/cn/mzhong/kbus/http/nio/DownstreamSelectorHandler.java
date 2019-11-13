package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

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
    protected void run(SelectionKey selectionKey) {
        if (selectionKey.isAcceptable()) {
            try {
                this.onAccept(selectionKey);
            } catch (IOException ignored) {
            }
        } else {
            super.run(selectionKey);
        }
    }

    @Override
    void onAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
        SelectableChannel accept = channel.accept().configureBlocking(false);
        this.register(accept, SelectionKey.OP_READ, null);
    }

    private HttpContext getContext(SelectionKey downstreamKey) {
        SocketChannel channel = (SocketChannel) downstreamKey.channel();
        HttpContext context = (HttpContext) downstreamKey.attachment();
        if (context == null) {
            synchronized (downstreamKey) {
                context = (HttpContext) downstreamKey.attachment();
                if (context == null) {
                    context = new HttpContext();
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
        HttpContext context = getContext(downstreamKey);
        SocketChannel downstream = context.getDownstream();
        HttpHeadReader headBuffer = context.getRequestHeadReader();
        ByteBuffer buffer = context.getInboundBuffer();
        if (buffer.position() != 0) {
            return;
        }
        int read = downstream.read(buffer);
        // 如果缓冲器读满，则停止监听读
        if (read == 0) {
            return;
        }
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
                    SocketChannel upstream = SocketChannel.open(new InetSocketAddress("localhost", 9000));
                    upstream.configureBlocking(false);
                    context.setUpstream(upstream);
                    String headString = new String(headBuffer.toBytes(), StandardCharsets.ISO_8859_1);
                    String[] split = headString.split("\r\n");
                    HttpRequestLine requestLine = HttpRequestLine.parse(split[0]);
                    HttpHeader header = HttpHeader.parse(split, 1);
                    header.set("Host", "182.151.197.163:5000");
                    header.set("Accept-Encoding", "deflate");
                    HttpRequestHead httpHead = new HttpRequestHead(requestLine, header);
                    HttpRequest request = new HttpRequest(httpHead);
                    context.setRequest(request);
                    context.setRequestWriter(new RequestWriter(context, header.getIntValue("Content-Length")));
                    this.upstreamHandler.append(context);
                    break;
                }
            }
        }
    }

    @Override
    void onWrite(SelectionKey downstreamKey) throws IOException {
        HttpContext context = (HttpContext) downstreamKey.attachment();
        SelectionKey upstreamKey = context.getUpstreamKey();
        HttpWriter responseWriter = context.getResponseWriter();
        ByteBuffer buffer = context.getResponse().getHead().getBuffer();
        // 读取head时多读出来的，先送上
        if (buffer.hasRemaining()) {
            responseWriter.writeHead(buffer);
        }
        if (buffer.hasRemaining()) {
            return;
        }
        buffer = context.getOutboundBuffer();
        if (responseWriter.writeBody(buffer) == IOStatus.EOF) {
            System.out.println("移除上下文：" + downstreamKey);
            upstreamKey.attach(null);
            upstreamKey.cancel();
            downstreamKey.attach(null);
            // 取消写事件监听
            downstreamKey.interestOps(upstreamKey.interestOps() & ~SelectionKey.OP_WRITE);
            this.wakeup();
            return;
        }
        // 如果写完了，换成上游读模式
        if (!buffer.hasRemaining()) {
            // 清除缓冲区，否则上游的数据读不进来
            buffer.clear();
        }
    }
}
