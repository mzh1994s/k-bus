package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 10:10
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioAcceptor extends AbstractHttpAcceptor {

    private final static Logger log = LoggerFactory.getLogger(HttpBioAcceptor.class);

    private Server server;
    private ExecutorService executor;
    private ServerSocket serverSocket;
    private int bufferSize;

    @Override
    public void start(Server server) throws IOException {
        this.server = server;
        KBus bus = server.getHttp().getBus();
        this.executor = bus.getExecutor();
        this.serverSocket = new ServerSocket(server.getListen());
        this.bufferSize = bus.getBufferSize();
        this.start();
    }

    private void start() {
        this.executor.execute(() -> {
            while (true) {
                try {
                    HttpBioAcceptor.this.accept(
                            HttpBioAcceptor.this.serverSocket.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void accept(Socket socket) {
        executor.execute(() -> {
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

    private HttpConnector createConnector(Location location) {
        AbstractHttpConnector connector = new HttpBioConnector(server);
        connector.setRequestWriter(new SimpleHttpRequestWriter());
        if (location.getChunkedTransferEncoding() == ChunkedTransferEncoding.ON) {
            connector.setResponseWriter(new SimpleHttpResponseWriter());
        } else {
            connector.setResponseWriter(new OffChunkedTransferEncodingHttpResponseWriter());
        }
        return connector;
    }

    private void acceptInternal(Socket socket) throws IOException {
//        socket.setSoTimeout(server.getTimeout());
        HttpLog httpLog = new HttpLog();
        HttpLog.threadLocal.set(httpLog);
        HttpDownStream downStream = new HttpDownStream(socket);
        HttpBioRequestReader requestReader = new HttpBioRequestReader(downStream, bufferSize);
        HttpRequest httpRequest;
        while ((httpRequest = requestReader.next()) != null) {
            Location location = HttpUriLocationMatcher.match(server.getLocations(), httpRequest.getRequestLine().getUri());
            if (location != null) {
                httpLog.setRequestLine(httpRequest.getRequestLine().getLine());
                HttpConnector connector = this.createConnector(location);
                try {
                    connector.connect(downStream, httpRequest, location);
                } catch (Exception ignored) {
                    socket.close();
                    break;
                }
            } else {
                socket.close();
            }
            if (log.isDebugEnabled()) {
                log.debug(httpLog.toString());
            }
        }
        HttpLog.threadLocal.remove();
        socket.close();
    }
}
