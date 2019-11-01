package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.http.*;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;
import cn.mzhong.kbus.http.header.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 10:10
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioAcceptor extends AbstractHttpAcceptor {

    private final static Logger log = LoggerFactory.getLogger(HttpBioAcceptor.class);

    private HttpBioUpstreamPool upstreamPool;
    private ServerSocket serverSocket;
    private int bufferSize;

    public HttpBioUpstreamPool getUpstreamPool() {
        return upstreamPool;
    }

    public void start() throws IOException {
        this.upstreamPool = super.beanFactory.getBioUpstreamPool();
        this.bufferSize = super.bus.getBufferSize();
        this.serverSocket = new ServerSocket(super.server.getListen());
        this.getExecutor().execute(() -> {
            while (true) {
                try {
                    HttpBioAcceptor.this.accept(
                            HttpBioAcceptor.this.serverSocket.accept());
                } catch (IOException e) {
                    log.debug(e.getLocalizedMessage(), e);
                }
            }
        });
    }


    private void accept(Socket socket) {
        getExecutor().execute(() -> {
            try {
                HttpBioAcceptor.this.acceptInternal(socket);
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    private HttpBioConnector createConnector(Location location) {
        HttpBioConnector connector = new HttpBioConnector(this);
        connector.setRequestWriter(new SimpleHttpBioRequestWriter());
        if (location.getChunkedTransferEncoding() == ChunkedTransferEncoding.ON) {
            connector.setResponseWriter(new SimpleHttpBioResponseWriter());
        } else {
            connector.setResponseWriter(new HttpBioResponseWriter_OffChunkedTransferEncoding());
        }
        return connector;
    }

    private void acceptInternal(Socket socket) throws IOException {
        HttpLog httpLog = new HttpLog();
        HttpLog.threadLocal.set(httpLog);
        HttpBioDownStream downStream = new HttpBioDownStream(socket);
        HttpBioRequestReader requestReader = new HttpBioRequestReader(downStream, bufferSize);
        HttpRequest httpRequest;
        try {
            while ((httpRequest = requestReader.next()) != null) {
                Location location = HttpUriLocationMatcher.match(getServer().getLocations(), httpRequest.getRequestLine().getUri());
                if (location != null) {
                    httpLog.setRequestLine(httpRequest.getRequestLine().getLine());
                    HttpBioConnector connector = this.createConnector(location);
                    connector.connect(downStream, httpRequest, location);
                } else {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug(httpLog.toString());
                }

                // 是否保持连接
                boolean doNext = false;
                if (HttpConstant.HTTP_1_1.equals(httpRequest.getRequestLine().getVersion())) {
                    doNext = true;
                }
                if (Connection.CLOSE.value.equals(httpRequest.getHeader().getString(HttpHeader.CONNECTION))) {
                    doNext = false;
                }
                if (!doNext) {
                    break;
                }
            }
        } finally {
            HttpLog.threadLocal.remove();
            downStream.close();
        }
    }
}
