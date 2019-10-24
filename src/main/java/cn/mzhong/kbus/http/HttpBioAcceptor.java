package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
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
        this.bufferSize = bus.getConfig().getBufferSize();
        this.connect();
    }

    public void connect() {
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

    private HttpConnector selectConnector() {
        return new HttpBioConnector(server);
    }

    private void acceptInternal(Socket socket) throws IOException {
        socket.setSoTimeout(1500);
        HttpLog httpLog = new HttpLog();
        HttpLog.threadLocal.set(httpLog);
        HttpBioParser parser = new HttpBioParser(socket, bufferSize);
        HttpRequest httpRequest;
        while ((httpRequest = parser.next()) != null) {
            HttpConnector connector = selectConnector();
            Location location = HttpUriLocationMatcher.match(server.getLocations(), httpRequest.getUri());
            if (location != null) {
                String proxyPass = location.getProxyPass();
                URL url = new URL(proxyPass);
                int port = url.getPort();
                try {
                    connector.connect(httpRequest, url.getHost(), port == -1 ? 80 : port);
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
