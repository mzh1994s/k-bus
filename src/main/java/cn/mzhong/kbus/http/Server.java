package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.IOType;

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
public class Server {
    /**
     * 监听的端口
     */
    private int listen;
    /**
     * 绑定的域名
     */
    private String serverName;

    private IOType ioType;
    /**
     * 等同于nginx的Location
     */
    private Set<Location> locations = new HashSet<>();

    private Http http;

    private HttpAcceptor acceptor;

    Server(Http http) {
        this.http = http;
    }

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

    public Http getHttp() {
        return http;
    }

    public void start() {
        this.acceptor = HttpAcceptor.getInstance(ioType);
        try {
            this.acceptor.start(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public Server addLocation(Location location) {
        this.locations.add(location);
        return this;
    }
}
