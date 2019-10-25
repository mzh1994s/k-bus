package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.SocketPool;
import cn.mzhong.kbus.http.header.Connection;
import cn.mzhong.kbus.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:14
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioConnector extends AbstractHttpConnector {

    private Server server;
    private SocketPool socketPool;
    private int bufferSize;

    HttpBioConnector(Server server) {
        this.server = server;
        KBus bus = server.getHttp().getBus();
        this.socketPool = bus.getSocketPool();
        this.bufferSize = bus.getBufferSize();
    }

    @Override
    public void connect(HttpRequest request, Location location) throws IOException {
        String host = location.getHost();
        int port = location.getPort();
        HttpLog httpLog = HttpLog.threadLocal.get();
        Connection connection = Connection.CLOSE;
        Socket socket = null;
        try {
            httpLog.start();
            socket = socketPool.getSocket(host, port);
            httpLog.saveConnectExpand();

            httpLog.start();
            HttpUpstream upstream = new HttpUpstream(socket);
            OutputStream sourceOut = request.getOutputStream();

            // 向目标服务器写入客户端传过来的请求头数据
            firstLineWriter.write(request, upstream, location);
            headerWriter.write(request, upstream, location);
            contentWriter.write(request, upstream, location);

            // 写完后就可以读取目标服务器响应的数据了

            int contentLength = 0;
            boolean isTransferEncoding = false;

            // 读取报文头
            String firstLine = StreamUtils.readLineAndWrite(socketIn, sourceOut);
            if (firstLine != null) {
                httpLog.setResponseLine(firstLine);
                String[] head = firstLine.split(" ");
                // HTTP/1.1 默认keepAlive为true
                connection = "HTTP/1.1".equals(head[0]) ? Connection.KEEP_ALIVE : Connection.CLOSE;
            } else {
                throw new IOException();
            }

            // 读header
            byte[] lineBytes;
            while ((lineBytes = StreamUtils.readLine(socketIn)) != null) {
                String line = new String(lineBytes);
                if (line.startsWith(HttpConstant.HEADER_PREFIX_TRANSFER_ENCODING)) {
                    isTransferEncoding = true;
                } else if (line.startsWith(HttpConstant.HEADER_PREFIX_CONTENT_LENGTH)) {
                    contentLength = Integer.parseInt(line.substring(HttpConstant.HEADER_PREFIX_CONTENT_LENGTH.length()));
                } else if (line.startsWith(HttpConstant.HEADER_PREFIX_CONNECTION)) {
                    connection = Connection.valueOfString(line.substring(HttpConstant.HEADER_PREFIX_CONNECTION.length()));
                }
                sourceOut.write(lineBytes);
                sourceOut.write(HttpConstant.LINE_SEPARATOR);
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
            connection = Connection.CLOSE;
            throw new IOException(e);
        } finally {
            if (socket != null) {
                if (connection == Connection.CLOSE) {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                }
                socketPool.returnSocket(socket);
            }
            httpLog.saveResponseExpand();
        }
    }
}
