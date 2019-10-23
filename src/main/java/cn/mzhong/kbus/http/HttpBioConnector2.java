package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.SocketPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:14
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioConnector2 implements HttpConnector {

    private final static Logger log = LoggerFactory.getLogger(HttpBioConnector2.class);

    private SocketPool socketPool;
    private ExecutorService executor;

    HttpBioConnector2(Server server) {
        KBus bus = server.getHttp().getBus();
        this.socketPool = bus.getSocketPool();
        this.executor = bus.getExecutor();
    }

    @Override
    public void connect(HttpRequest request, String host, int port) throws IOException {
        Socket socket = null;
        try {
            socket = socketPool.getSocket(host, port);
            InputStream socketIn = socket.getInputStream();
            OutputStream socketOut = socket.getOutputStream();
            OutputStream sourceOut = request.getOutputStream();

            // 写
            socketOut.write(request.getData());
            socketOut.flush();

            // 读
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.ISO_8859_1));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sourceOut, StandardCharsets.ISO_8859_1));

            String line;
            int contentLength = 0;
            boolean isTransferEncoding = false;
            long start = System.currentTimeMillis();
            // 读header
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Transfer-Encoding: ")) {
                    isTransferEncoding = true;
                } else if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring(16));
                }
                writer.write(line);
                writer.write(HttpConstant.LINE_SEPARATOR);
                if ("".equals(line)) {
                    break;
                }
            }
            writer.flush();
            if (isTransferEncoding) {
                // 读数据块
                char[] eof = new char[5];
                int read;
                while ((read = reader.read()) != -1) {
                    eof[4] = (char) read;
                    writer.write(eof[4]);
                    if (eof[0] == '0'
                            && eof[1] == '\r'
                            && eof[2] == '\n'
                            && eof[3] == '\r'
                            && eof[4] == '\n') {
                        break;
                    }
                    eof[0] = eof[1];
                    eof[1] = eof[2];
                    eof[2] = eof[3];
                    eof[3] = eof[4];
                }
            } else if (contentLength != 0) {
                // 读content
                char[] chars = new char[1024];
                for (int len = 0, read; len < contentLength; len += read) {
                    read = reader.read(chars);
                    writer.write(chars, 0, read);
                }
            }
            writer.flush();
            if (log.isDebugEnabled()) {
                log.debug("接收耗时：" + (System.currentTimeMillis() - start));
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socketPool.returnSocket(socket);
                } catch (Exception e) {

                }
            }
        }
    }

    
}
