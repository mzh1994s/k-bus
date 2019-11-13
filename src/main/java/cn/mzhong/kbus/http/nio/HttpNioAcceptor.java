package cn.mzhong.kbus.http.nio;

import cn.mzhong.kbus.http.AbstractHttpAcceptor;
import cn.mzhong.kbus.http.Server;

import java.io.IOException;

/**
 * TODO<br>
 * 创建时间： 2019/10/29 9:31
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpNioAcceptor extends AbstractHttpAcceptor {

    private DownstreamSelectorHandler downstreamHandler;
    private UpstreamSelectorHandler upstreamHandler;
    HttpNioConnectionFactory connectionFactory;

    @Override
    protected void start() throws IOException {
        Server server = getServer();

        // 创建上下游处理器
        this.downstreamHandler = new DownstreamSelectorHandler(server);
        this.upstreamHandler = new UpstreamSelectorHandler(server);
        this.downstreamHandler.setUpstreamHandler(this.upstreamHandler);
        this.upstreamHandler.setDownstreamHandler(this.downstreamHandler);
        this.downstreamHandler.start();
        this.upstreamHandler.start();

        // 其他
        this.connectionFactory = new HttpNioConnectionFactory();
    }
}
