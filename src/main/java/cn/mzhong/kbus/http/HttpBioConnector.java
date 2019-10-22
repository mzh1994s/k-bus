package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.SocketPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:14
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioConnector implements HttpConnector {

    private final static Logger log = LoggerFactory.getLogger(HttpBioConnector.class);

    private SocketPool socketPool;
    private ExecutorService executor;

    HttpBioConnector(Server server) {
        KBus bus = server.getHttp().getBus();
        this.socketPool = bus.getSocketPool();
        this.executor = bus.getExecutor();
    }

    @Override
    public void connect(HttpRequest request, String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port));
        try (InputStream socketIn = socket.getInputStream();
             OutputStream socketOut = socket.getOutputStream();
             InputStream sourceIn = request.getInputStream();
             OutputStream sourceOut = request.getOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = sourceIn.read(buf)) != -1) {
                socketOut.write(buf, 0, len);
            }
            socketOut.flush();
            long start = System.currentTimeMillis();
            while ((len = socketIn.read(buf)) != -1) {
                sourceOut.write(buf, 0, len);
            }
            sourceOut.flush();
            if (log.isDebugEnabled()) {
                log.debug("接收耗时：" + (System.currentTimeMillis() - start));
            }
        } catch (Exception ignored) {
        }
    }
}
