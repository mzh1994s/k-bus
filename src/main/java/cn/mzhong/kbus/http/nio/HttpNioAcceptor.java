package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    HttpNioConnectionFactory connectionFactory;
    private Queue<RequestContext> requestContextList;
    private List<RequestHandler> requestHandlerList;

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
        this.connectionFactory = new HttpNioConnectionFactory();
        this.requestContextList = new ConcurrentLinkedQueue<>();
        this.initRequestHandlerSet();
        this.downstreamListen();
        this.upstreamListen();
    }

    private void initRequestHandlerSet() {
        this.requestHandlerList = new LinkedList<>();
        this.requestHandlerList.add(new HttpRequestHandler());
    }

    private Set<SelectionKey> await(Selector selector) {
        try {
            selector.select();
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
                            ServerSocketChannel channel = (ServerSocketChannel) next.channel();
                            SocketChannel accept = channel.accept();
                            accept.configureBlocking(false);
                            RequestContext context = new RequestContext();
                            context.setDownstream(accept);
                            context.setDownstreamKey(accept.register(this.downstreamSelector, SelectionKey.OP_READ, context));
                            System.out.println("创建连接：" + context.getDownstreamKey());
                        } else if (next.isReadable()) {
                            this.onDownstreamRead(next);
                        } else if (next.isWritable()) {
                            this.onDownstreamWrite(next);
                        }
                    } catch (Exception e) {
                        next.cancel();
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
                            this.onUpstreamRead(next);
                        } else if (next.isWritable()) {
                            this.onUpstreamWrite(next);
                        }
                    } catch (Exception e) {
                        next.cancel();
                    }
                }
                RequestContext poll;
                while ((poll = requestContextList.poll()) != null) {
                    try {
                        poll.setUpstreamKey(
                                poll.getUpstream().register(this.upstreamSelector, SelectionKey.OP_WRITE, poll));
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private RequestContext getContext(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        if (context == null) {
            synchronized (selectionKey) {
                context = (RequestContext) selectionKey.attachment();
                if (context == null) {
                    context = new RequestContext();
                    context.setDownstreamKey(selectionKey);
                    context.setDownstream(channel);
                    selectionKey.attach(context);
                }
            }
        }
        return context;
    }

    protected void onDownstreamWrite(SelectionKey downstreamKey) throws IOException {
        RequestContext context = (RequestContext) downstreamKey.attachment();
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
        buffer = context.getBuffer();
        if (responseWriter.writeBody(buffer) == IOStatus.EOF) {
            System.out.println("移除上下文：" + downstreamKey);
            upstreamKey.attach(null);
            upstreamKey.cancel();
            downstreamKey.attach(null);
            // 监听下一次请求
            downstreamKey.interestOps(SelectionKey.OP_READ);
            return;
        }
        // 如果写完了，换成上游读模式
        if (!buffer.hasRemaining()) {
            // 下游禁写
            downstreamKey.interestOps(0);
            // 上游置读
            upstreamKey.interestOps(SelectionKey.OP_READ);
        }
    }

    protected void onDownstreamRead(SelectionKey downstreamKey) throws IOException {
        RequestContext context = getContext(downstreamKey);
        SocketChannel downstream = context.getDownstream();
        HttpHeadReader headBuffer = context.getRequestHeadReader();
        ByteBuffer buffer = context.getBuffer();
        int read = downstream.read(context.getBuffer());
        // 如果缓冲器读满，则停止监听读
        if (read == 0) {
            return;
        }
        if (read == -1) {
            throw new IOClosedException();
        }
        buffer.flip();
        if (headBuffer.isEof()) {
            // 下游禁读
            downstreamKey.interestOps(0);
            // 上游启写
            SelectionKey upstreamKey = context.getUpstreamKey();
            upstreamKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            // 读head
            while (buffer.hasRemaining()) {
                headBuffer.add(buffer.get());
                if (headBuffer.isEof()) {
                    SocketChannel upstream = connectionFactory.getConnection("182.151.197.163", 5000);
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
                    // 下游禁读
                    downstreamKey.interestOps(0);
                    this.requestContextList.add(context);
                    this.upstreamSelector.wakeup();
                    break;
                }
            }
        }
    }

    private void onUpstreamWrite(SelectionKey upstreamKey) throws IOException {
        RequestContext context = (RequestContext) upstreamKey.attachment();
        // 看头有没有传完，传完头再传数据
        ByteBuffer buffer = context.getRequest().getHead().getBuffer();
        if (buffer.hasRemaining()) {
            context.getRequestWriter().writeHead(buffer);
        }
        // 如果没写完，放入下一次继续
        if (buffer.hasRemaining()) {
            return;
        }
        buffer = context.getBuffer();
        if (context.getRequestWriter().writeBody(buffer) == IOStatus.EOF) {
            // 上游启读
            upstreamKey.interestOps(SelectionKey.OP_READ);
            // 下游禁读写
            SelectionKey downstreamKey = context.getDownstreamKey();
            downstreamKey.interestOps(0);
            // 缓冲区清除干净，准备接受响应数据
            buffer.clear();
        }
    }

    protected void onUpstreamRead(SelectionKey upstreamKey) throws IOException {
        SocketChannel upstream = (SocketChannel) upstreamKey.channel();
        RequestContext context = (RequestContext) upstreamKey.attachment();
        HttpHeadReader headBuffer = context.getResponseHeadReader();
        ByteBuffer buffer = context.getBuffer();
        int read = upstream.read(buffer);
        if (read == 0) {
            return;
        }
        if (read == -1) {
            throw new IOClosedException();
        }
        buffer.flip();
        SelectionKey downstreamKey = context.getDownstreamKey();
        if (headBuffer.isEof()) {
            // 下游写
            downstreamKey.interestOps(SelectionKey.OP_WRITE);
            upstreamKey.interestOps(0);
        } else {
            while (buffer.hasRemaining()) {
                headBuffer.add(buffer.get());
                if (headBuffer.isEof()) {
                    String headString = new String(headBuffer.toBytes(), StandardCharsets.ISO_8859_1);
                    String[] split = headString.split("\r\n");
                    HttpResponseLine responseLine = HttpResponseLine.parse(split[0]);
                    HttpHeader header = HttpHeader.parse(split, 1);
                    HttpResponseHead httpResponseHead = new HttpResponseHead(responseLine, header);
                    HttpResponse response = new HttpResponse(httpResponseHead);
                    context.setResponse(response);

                    int contentLength = header.getIntValue("Content-Length");
                    HttpWriter responseWriter;
                    if (contentLength > 0) {
                        responseWriter = new SimpleResponseWriter(context, contentLength);
                    } else {
                        responseWriter = new TruckResponseWriter(context);
                    }
                    context.setResponseWriter(responseWriter);
                    // 响应头接受完毕了，通知下游读
                    downstreamKey.interestOps(SelectionKey.OP_WRITE);
                    upstreamKey.interestOps(0);
                    break;
                }
            }
        }
    }
}
