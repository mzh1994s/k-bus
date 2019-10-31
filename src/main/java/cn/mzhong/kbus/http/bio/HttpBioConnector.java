package cn.mzhong.kbus.http.bio;

import cn.mzhong.kbus.core.KBus;
import cn.mzhong.kbus.http.*;
import cn.mzhong.kbus.http.header.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 11:14
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpBioConnector extends AbstractHttpConnector {


    private final static Logger log = LoggerFactory.getLogger(HttpBioConnector.class);

    private Server server;
    private HttpBioUpstreamPool upstreamPool;
    private int bufferSize;
    private HttpBioResponseReader responseReader = new HttpBioResponseReader();

    HttpBioConnector(HttpBioAcceptor acceptor) {
        this.server = acceptor.getServer();
        KBus bus = server.getHttp().getBus();
        this.upstreamPool = acceptor.getUpstreamPool();
        this.bufferSize = bus.getBufferSize();
    }

    public void connect(HttpBioDownStream downStream, HttpRequest request, Location location) throws IOException {
        HttpBioRequest bioRequest = (HttpBioRequest) request;
        String host = location.getHost();
        int port = location.getPort();
        HttpLog httpLog = HttpLog.threadLocal.get();
        Connection connection = Connection.CLOSE;
        HttpBioUpstream upstream = null;
        boolean retry;
        do {
            retry = false;
            try {
                httpLog.start();
                upstream = upstreamPool.get(host, port);
                httpLog.saveConnectExpand();
                // 向上游服务器写入客户端传过来的请求头数据
                requestWriter.write(bioRequest, upstream, location);

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
                log.debug(e.getLocalizedMessage(), e);
                // 远程服务器关闭了通道，需要重新打开新通道
                retry = true;
                connection = Connection.CLOSE;
            } catch (Exception e) {
                log.debug(e.getLocalizedMessage(), e);
                // 遇到异常时也要关闭连接
                connection = Connection.CLOSE;
                throw new IOException(e);
            } finally {
                if (upstream != null) {
                    if (connection == Connection.CLOSE) {
                        upstream.close();
                    }
                    // 无论是否关闭都要归还Socket
                    upstreamPool.back(upstream);
                }
                httpLog.saveResponseExpand();
            }
        } while (retry);
    }
}
