package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.HttpResponse;
import cn.mzhong.kbus.http.HttpResponseHead;
import cn.mzhong.kbus.http.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
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

    private SelectorHandler downstreamHandler;
    private final Queue<HttpNioContext> contextQueue = new ConcurrentLinkedQueue<>();

    public UpstreamSelectorHandler(Server server) {
        super(server);
    }

    public void setDownstreamHandler(SelectorHandler downstreamHandler) {
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    void onSelected(Set<SelectionKey> selectionKeys) {
        HttpNioContext context;
        while ((context = contextQueue.poll()) != null) {
            try {
                context.setUpstreamKey(this.register(context.getUpstream(),
                        SelectionKey.OP_WRITE | SelectionKey.OP_READ, context));
            } catch (ClosedChannelException ignored) {
            }
        }
    }

    @Override
    void onRead(SelectionKey upstreamKey) throws IOException {
        SocketChannel upstream = (SocketChannel) upstreamKey.channel();
        HttpNioContext context = (HttpNioContext) upstreamKey.attachment();
        HttpHeadReader headBuffer = context.getResponseHeadReader();
        ByteBuffer buffer = context.getOutboundBuffer();
        buffer.clear();
        int read = upstream.read(buffer);
        if (read == -1) {
            throw new IOClosedException();
        }
        buffer.flip();
        SelectionKey downstreamKey = context.getDownstreamKey();
        if (headBuffer.isEof()) {
            // 下游写
            downstreamKey.interestOps(downstreamKey.interestOps() | SelectionKey.OP_WRITE);
            this.downstreamHandler.wakeup();
        } else {
            while (buffer.hasRemaining()) {
                headBuffer.add(buffer.get());
                if (headBuffer.isEof()) {
                    HttpResponseHead httpResponseHead = HttpResponseHead.parse(headBuffer);
                    HttpResponse response = new HttpResponse(httpResponseHead);
                    context.setResponse(response);

                    int contentLength = httpResponseHead.getHeader().getIntValue("Content-Length");
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

    /**
     * 从{@link #onWrite(SelectionKey)}中拆分出的方法，当本次写操作没有把缓冲区的
     * 数据写完时，让写事件延续到缓冲区的数据写完为止。
     *
     * @param upstreamKey
     */
    private void keepWrite(SelectionKey upstreamKey) {
        upstreamKey.interestOps(upstreamKey.interestOps() | SelectionKey.OP_WRITE);
        this.wakeup();
    }

    private void finishWrite(SelectionKey downstreamKey) {
        // 清除缓冲区
        downstreamKey.interestOps(downstreamKey.interestOps() | SelectionKey.OP_READ);
        this.downstreamHandler.wakeup();
    }

    @Override
    void onWrite(SelectionKey upstreamKey) throws IOException {
        HttpNioContext context = (HttpNioContext) upstreamKey.attachment();
        SelectionKey downstreamKey = context.getDownstreamKey();
        // 看头有没有传完，传完头再传数据
        ByteBuffer buffer = context.getRequest().getHead().getBuffer();
        if (buffer.hasRemaining()) {
            context.getRequestWriter().writeHead(buffer);
        }
        if (buffer.hasRemaining()) {
            // 没写完，保持写事件
            this.keepWrite(upstreamKey);
            return;
        }
        buffer = context.getInboundBuffer();
        if (context.getRequestWriter().writeBody(buffer) == IOStatus.EOF) {
            this.finishWrite(downstreamKey);
            return;
        }
        if (buffer.hasRemaining()) {
            // 没写完，保持写事件
            this.keepWrite(upstreamKey);
        } else {
            this.finishWrite(downstreamKey);
        }
    }

    void append(HttpNioContext context) {
        contextQueue.add(context);
        this.wakeup();
    }
}
