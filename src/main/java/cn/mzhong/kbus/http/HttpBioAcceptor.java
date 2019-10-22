package cn.mzhong.kbus.http;

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

    private Server server;
    private ExecutorService executor;
    private ServerSocket serverSocket;

    @Override
    public void start(Server server) throws IOException {
        this.server = server;
        this.executor = server.getHttp().getBus().getExecutor();
        this.serverSocket = new ServerSocket(server.getListen());
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
        HttpBioParser parser = new HttpBioParser(socket);
        HttpRequest httpRequest = parser.doParse();
        HttpConnector connector = selectConnector();
        Location location = HttpUriLocationMatcher.match(server.getLocations(), httpRequest.getUri());
        if (location != null) {
            String proxyPass = location.getProxyPass();
            URL url = new URL(proxyPass);
            int port = url.getPort();
            connector.connect(httpRequest, url.getHost(), port == -1 ? 80 : port);
        } else {
            socket.close();
        }
    }
}
