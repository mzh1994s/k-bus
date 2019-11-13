package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 上游处理器<br>
 * 创建时间： 2019/11/13 14:43
 *
 * @author mzhong
 * @version 1.0
 */
public class UpstreamSelectorHandler extends SelectorHandler {

    SelectorHandler downstreamHandler;
    private final Queue<HttpContext> contextQueue = new ConcurrentLinkedQueue<>();

    public UpstreamSelectorHandler(Server server) {
        super(server);
    }

    public void setDownstreamHandler(SelectorHandler downstreamHandler) {
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    void onSelected(Set<SelectionKey> selectionKeys) {
        HttpContext context;
        while ((context = contextQueue.poll()) != null) {
            try {
                context.setUpstreamKey(this.register(context.getUpstream(), SelectionKey.OP_WRITE, context));
            } catch (ClosedChannelException ignored) {
            }
        }
    }

    @Override
    void onRead(SelectionKey upstreamKey) throws IOException {
        SocketChannel upstream = (SocketChannel) upstreamKey.channel();
        HttpContext context = (HttpContext) upstreamKey.attachment();
        HttpHeadReader headBuffer = context.getResponseHeadReader();
        ByteBuffer buffer = context.getOutboundBuffer();
        // 有数据没写完就不读
        if (buffer.position() != 0) {
            return;
        }
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
//            downstreamKey.interestOps(downstreamKey.interestOps() | SelectionKey.OP_WRITE);
//            this.downstreamHandler.wakeup();
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
                    downstreamKey.interestOps(downstreamKey.interestOps() | SelectionKey.OP_WRITE);
                    this.downstreamHandler.wakeup();
                    break;
                }
            }
        }
    }

    @Override
    void onWrite(SelectionKey upstreamKey) throws IOException {
        HttpContext context = (HttpContext) upstreamKey.attachment();
        // 看头有没有传完，传完头再传数据
        ByteBuffer buffer = context.getRequest().getHead().getBuffer();
        if (buffer.hasRemaining()) {
            context.getRequestWriter().writeHead(buffer);
        }
        // 如果没写完，放入下一次继续
        if (buffer.hasRemaining()) {
            return;
        }
        buffer = context.getInboundBuffer();
        if (context.getRequestWriter().writeBody(buffer) == IOStatus.EOF) {
            // 取消写事件监听
            upstreamKey.interestOps(upstreamKey.interestOps() & ~SelectionKey.OP_WRITE);
            // 清除缓冲区
            buffer.clear();
        }
    }

    void append(HttpContext context) {
        contextQueue.add(context);
        this.wakeup();
    }
}
