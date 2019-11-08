package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    ConnectionFactory connectionFactory;
    List<RequestContext> requestContextList;

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
        this.connectionFactory = new ConnectionFactory();
        this.requestContextList = new LinkedList<>();
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
                Iterator<RequestContext> contextIterator = requestContextList.iterator();
                while (contextIterator.hasNext()) {
                    RequestContext context = contextIterator.next();
                    contextIterator.remove();
                    try {
                        context.setUpstreamKey(
                                context.getUpstream().register(this.upstreamSelector, SelectionKey.OP_READ, context));
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void downstreamRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        System.out.println("上下文获取：" + selectionKey);
        RequestContext context = (RequestContext) selectionKey.attachment();
        if (context == null) {
            context = new RequestContext();
            context.setDownstreamKey(selectionKey);
            context.setDownstream(channel);
            selectionKey.attach(context);
        }
        HttpHeadBuffer headBuffer = context.getRequestHeadBuffer();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int read;
            while ((read = channel.read(buffer)) > 0) {
                buffer.flip();
                if (headBuffer.isEof()) {
                    context.getUpstream().write(buffer);
                } else {
                    while (buffer.hasRemaining()) {
                        headBuffer.add(buffer.get());
                        if (headBuffer.isEof()) {
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
                            System.out.print(new String(httpHead.toByteArray()));
                            ByteBuffer requestBuffer = ByteBuffer.wrap(httpHead.toByteArray());
                            upstream.write(requestBuffer);
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
                        System.out.println();
                        System.out.println("上下文移除：" + context.getDownstreamKey());
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
                            HttpHeader header = new HttpHeader();
                            int length = split.length;
                            for (int i = 1; i < length; i++) {
                                header.putLine(split[i]);
                            }
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
                            System.out.print(new String(httpResponseHead.toByteArray()));
                            responseWriter.writeHead(ByteBuffer.wrap(httpResponseHead.toByteArray()));
                            // 读取head时多读出来的，先送上
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            while (buffer.hasRemaining()) {
                                byteArrayOutputStream.write(buffer.get());
                            }
                            responseWriter.writeBody(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
                        }
                    }
                }
                buffer.clear();
            }
            if (read == -1) {
                selectionKey.cancel();
            }
        } catch (Exception e) {
            selectionKey.cancel();
        }
    }
}
