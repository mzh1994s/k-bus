package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.IOType;
import cn.mzhong.kbus.core.Reloadable;
import cn.mzhong.kbus.core.Startable;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 15:19
 *
 * @author mzhong
 * @version 1.0
 */
public class Server implements Startable, Reloadable {

    /**------------------------基准配置项 start*/

    /**
     * 监听的端口
     */
    private int listen;
    /**
     * 绑定的域名
     */
    private String serverName;
    /**
     * IO类型
     */
    private IOType io;

    /**
     * 连接超时时间（上游）
     */
    private int timeout = 1500;

    /**
     * 默认值取{@link Http#getChunkedTransferEncoding()}
     */
    private ChunkedTransferEncoding chunkedTransferEncoding;

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public IOType getIo() {
        return io;
    }

    public void setIo(IOType io) {
        this.io = io;
    }

    public int getTimeout() {
        return timeout;
    }

    public ChunkedTransferEncoding getChunkedTransferEncoding() {
        return chunkedTransferEncoding;
    }

    public Server setChunkedTransferEncoding(ChunkedTransferEncoding chunkedTransferEncoding) {
        this.config.chunkedTransferEncoding = chunkedTransferEncoding;
        return this;
    }

    /**
     * ------------------------基准配置项 end
     */

    private Config config = new Config();

    /**
     * 等同于nginx的Location
     */
    private Set<Location> locations = new HashSet<>();

    private Http http;

    private HttpAcceptor acceptor;

    Server(Http http) {
        this.http = http;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public Location createLocation() {
        Location location = new Location(this);
        this.locations.add(location);
        return location;
    }

    public Http getHttp() {
        return http;
    }

    @Override
    public void start() {
        for (Location location : this.locations) {
            location.start();
        }
        this.acceptor = HttpAcceptor.getInstance(io);
        try {
            this.acceptor.start(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        this.chunkedTransferEncoding = this.config.chunkedTransferEncoding == null ?
                http.getChunkedTransferEncoding() : this.config.chunkedTransferEncoding;
    }

    private static class Config {
        private ChunkedTransferEncoding chunkedTransferEncoding;
    }
}
