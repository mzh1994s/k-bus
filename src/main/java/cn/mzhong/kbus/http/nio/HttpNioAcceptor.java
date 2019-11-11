package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
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
    Queue<RequestContext> requestContextList;

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
        this.downstreamListen();
        this.upstreamListen();
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
                            downstreamRead(next);
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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                RequestContext poll;
                while ((poll = requestContextList.poll()) != null) {
                    try {
                        poll.setUpstreamKey(
                                poll.getUpstream().register(this.upstreamSelector, SelectionKey.OP_READ, poll));
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void downstreamRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        if (context == null) {
            System.out.println("创建上下文：" + selectionKey);
            context = new RequestContext();
            context.setDownstreamKey(selectionKey);
            context.setDownstream(channel);
            selectionKey.attach(context);
        }
        if (context.isRequested()) {
            return;
        }
        HttpHeadBuffer headBuffer = context.getRequestHeadBuffer();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int read;
            while ((read = channel.read(buffer)) > 0) {
                buffer.flip();
                if (headBuffer.isEof()) {
                    while (buffer.hasRemaining()) {
                        context.getUpstream().write(buffer);
                    }
                } else {
                    while (buffer.hasRemaining()) {
                        headBuffer.add(buffer.get());
                        if (headBuffer.isEof()) {
                            context.setRequested(true);
                            SocketChannel upstream = connectionFactory.getConnection("182.151.197.163", 5000);
                            context.setUpstream(upstream);
                            this.requestContextList.add(context);
                            this.upstreamSelector.wakeup();
                            String headString = new String(headBuffer.toBytes(), StandardCharsets.ISO_8859_1);
                            String[] split = headString.split("\r\n");
                            HttpRequestLine requestLine = HttpRequestLine.parse(split[0]);
                            HttpHeader header = HttpHeader.parse(split, 1);
                            header.set("Host", "182.151.197.163:5000");
                            header.set("Accept-Encoding", "deflate");
                            HttpRequestHead httpHead = new HttpRequestHead(requestLine, header);
                            HttpRequest request = new HttpRequest(httpHead);
                            context.setRequest(request);
                            ByteBuffer requestBuffer = ByteBuffer.wrap(httpHead.toByteArray());
                            while (requestBuffer.hasRemaining()) {
                                upstream.write(requestBuffer);
                            }
                            while (buffer.hasRemaining()) {
                                upstream.write(buffer);
                            }
                        }
                    }
                }
                buffer.clear();
            }
            if (read == -1) {
                System.out.println("关闭连接：" + selectionKey);
                selectionKey.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            selectionKey.cancel();
        }
    }

    protected void upstreamRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RequestContext context = (RequestContext) selectionKey.attachment();
        HttpHeadBuffer headBuffer = context.getResponseHeadBuffer();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read;
        try {
            while ((read = channel.read(buffer)) > 0) {
                buffer.flip();
                if (headBuffer.isEof()) {
                    if (context.getResponseWriter().writeBody(buffer) == -1) {
                        System.out.println("移除上下文：" + context.getDownstreamKey());
                        context.getUpstreamKey().attach(null);
                        context.getDownstreamKey().attach(null);
                        selectionKey.cancel();
                    }
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

                            long contentLength = header.getIntValue("Content-Length");
                            ResponseWriter responseWriter;
                            if (contentLength > 0) {
                                responseWriter = new SimpleResponseWriter(context, contentLength);
                            } else {
                                responseWriter = new TruckResponseWriter(context);
                            }
                            context.setResponseWriter(responseWriter);
                            responseWriter.writeHead(ByteBuffer.wrap(httpResponseHead.toByteArray()));
                            // 读取head时多读出来的，先送上
                            if (responseWriter.writeBody(buffer) == -1) {
                                System.out.println("移除上下文：" + context.getDownstreamKey());
                                context.getUpstreamKey().attach(null);
                                context.getDownstreamKey().attach(null);
                                selectionKey.cancel();
                            }
                        }
                    }
                }
                buffer.clear();
            }
            if (read == -1) {
                selectionKey.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            selectionKey.cancel();
        }
    }
}
