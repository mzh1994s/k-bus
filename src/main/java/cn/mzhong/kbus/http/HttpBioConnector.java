package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.core.SocketPool;
import cn.mzhong.kbus.http.header.Connection;

import java.io.IOException;
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
    private HttpBioResponseReader responseReader = new HttpBioResponseReader();

    HttpBioConnector(Server server) {
        this.server = server;
        KBus bus = server.getHttp().getBus();
        this.socketPool = bus.getSocketPool();
        this.bufferSize = bus.getBufferSize();
    }

    public void connect(HttpDownStream downStream, HttpRequest request, Location location) throws IOException {

        String host = location.getHost();
        int port = location.getPort();
        HttpLog httpLog = HttpLog.threadLocal.get();
        Connection connection = Connection.CLOSE;
        Socket socket = null;
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                httpLog.start();
                socket = socketPool.getSocket(host, port);
                httpLog.saveConnectExpand();

                httpLog.start();
                HttpUpstream upstream = new HttpUpstream(socket);

                // 向目标服务器写入客户端传过来的请求头数据
                requestWriter.write(request, upstream, location);

                // 写完后就可以读取目标服务器响应的数据了
                HttpResponse response = responseReader.read(upstream.getInputStream());
                httpLog.setResponseLine(response.getResponseLine().getLine());
                HttpHeader responseHeader = response.getHeader();

                // http1.1默认KEEP_ALIVE
                if (HttpConstant.HTTP_1_1.equals(response.getResponseLine().getVersion())) {
                    connection = Connection.KEEP_ALIVE;
                }
                String headerConnection = responseHeader.getString(HttpHeader.CONNECTION);
                if (headerConnection != null) {
                    connection = Connection.valueOfString(headerConnection);
                }
                responseWriter.write(upstream, response, downStream, location);
            } catch (IOEOFException e) {
                // 远程服务器关闭了通道，需要重新打开新通道
                retry = true;
                connection = Connection.CLOSE;
            } catch (Exception e) {
                e.printStackTrace();
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
}
