package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.SocketPool;
import cn.mzhong.kbus.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private int bufferSize;

    HttpBioConnector(Server server) {
        KBus bus = server.getHttp().getBus();
        this.socketPool = bus.getSocketPool();
        this.executor = bus.getExecutor();
        this.bufferSize = bus.getConfig().getBufferSize();
    }

    @Override
    public void connect(HttpRequest request, String host, int port) throws IOException {
        HttpLog httpLog = HttpLog.threadLocal.get();
        boolean isKeepAlive = false;
        Socket socket = null;
        InputStream socketIn;
        OutputStream socketOut;
        try {
            httpLog.start();
            socket = socketPool.getSocket(host, port);
            httpLog.saveConnectExpand();

            httpLog.start();
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            OutputStream sourceOut = request.getOutputStream();

            // 向目标服务器写入客户端传过来的请求头数据
            socketOut.write(request.getData());
            socketOut.flush();

            // 写完后就可以读取目标服务器响应的数据了
            String line;
            int contentLength = 0;
            boolean isTransferEncoding = false;

            // 读取报文头
            String firstLine = StreamUtils.readLineAndWrite(socketIn, sourceOut);
            if (firstLine != null) {
                httpLog.setResponseLine(firstLine);
                String[] head = firstLine.split(" ");
                // HTTP/1.1 默认keepAlive为true
                isKeepAlive = "HTTP/1.1".equals(head[0]);
            } else {
                throw new IOException();
            }

            // 读header
            while ((line = StreamUtils.readLineAndWrite(socketIn, sourceOut)) != null) {
                if (line.startsWith("Transfer-Encoding: ")) {
                    isTransferEncoding = true;
                } else if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring(16));
                } else if (line.startsWith("Connection: ")) {
                    isKeepAlive = !"close".equals(line.substring(12));
                }
                if (line.isEmpty()) {
                    break;
                }
            }
            // flush一下，让客户端率先知道请求头
            sourceOut.flush();

            // 传输响应体，响应体有两种。
            // 一种是header中有Content-Length字段的可以直接读取Content-Length大小的数据即可
            // 还有一种响应体是分块格式的数据，用Transfer-Encoding字段辨识
            if (isTransferEncoding) {
                // 读数据块
                byte[] eof = new byte[5];
                int read;
                while ((read = socketIn.read()) != -1) {
                    eof[4] = (byte) read;
                    sourceOut.write(eof[4]);
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
            } else if (contentLength > 0) {
                StreamUtils.copy(socketIn, sourceOut, bufferSize, contentLength);
            }
            sourceOut.flush();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (socket != null) {
                if (!isKeepAlive) {
                    socket.close();
                }
                socketPool.returnSocket(socket);
            }
            httpLog.saveResponseExpand();
        }
    }


}
