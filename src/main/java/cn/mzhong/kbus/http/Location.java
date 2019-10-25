package cn.mzhong.kbus.http;

import cn.mzhong.kbus.core.Reloadable;
import cn.mzhong.kbus.core.Startable;
import cn.mzhong.kbus.http.conf.ChunkedTransferEncoding;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 15:32
 *
 * @author mzhong
 * @version 1.0
 */
public class Location implements Startable, Reloadable {
    private Server server;
    private String value;

    private String protocol;
    private String host;
    private int port;
    private String path;
    private ChunkedTransferEncoding chunkedTransferEncoding;
    private Config config = new Config();

    Location(Server server) {
        this.server = server;
    }

    public String getValue() {
        return value;
    }

    public Location setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * 必须是标准的地址，protocol://host:port
     *
     * @param proxyPass
     * @return
     */
    public Location setProxyPass(String proxyPass) {

        int start;

        // 解析协议部分
        int indexOf = proxyPass.indexOf("://");
        if (indexOf == -1) {
            throw new RuntimeException("找不到协议部分：" + proxyPass);
        }
        // http://mzhong.cn/
        this.protocol = proxyPass.substring(0, indexOf);
        // 计算host部分的开头
        start = indexOf + 3;

        // 解析host部分
        // mzhong.cn/
        String newStr = proxyPass.substring(start);
        indexOf = newStr.indexOf(':');
        if (indexOf == -1) {
            int indexOf2 = newStr.indexOf('/');
            if (indexOf2 == -1) {
                // mzhong.cn
                if (!newStr.isEmpty()) {
                    this.host = newStr;
                    start = newStr.length();
                } else {
                    throw new RuntimeException("找不到Host部分：" + proxyPass);
                }
            } else {
                // mzhong.cn/
                this.host = newStr.substring(0, indexOf2);
                start = indexOf2;
            }
        } else {
            // mzhong.cn:80
            this.host = newStr.substring(0, indexOf);
            start = indexOf + 1;
        }

        // 解析端口部分
        if (start > newStr.length()) {
            this.port = getPortFromProtocol();
            start = 0;
        } else {
            newStr = newStr.substring(start);
            indexOf = newStr.indexOf('/');
            if (indexOf == -1) {
                if (newStr.isEmpty()) {
                    // empty
                    this.port = getPortFromProtocol();
                    start = 0;
                } else {
                    // 80
                    this.port = Integer.parseInt(newStr);
                    start = newStr.length();
                }
            } else {
                if (indexOf != 0) {
                    // 80/
                    this.port = Integer.parseInt(newStr.substring(0, indexOf));
                    start = indexOf;
                } else {
                    // empty/
                    this.port = getPortFromProtocol();
                    start = 0;
                }
            }
        }

        // 解析path部分
        if (start > newStr.length()) {
            this.path = "/";
        } else {
            String path = newStr.substring(start);
            this.path = path.isEmpty() ? "/" : path;
        }
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public ChunkedTransferEncoding getChunkedTransferEncoding() {
        return chunkedTransferEncoding;
    }

    public Location setChunkedTransferEncoding(ChunkedTransferEncoding chunkedTransferEncoding) {
        this.config.chunkedTransferEncoding = chunkedTransferEncoding;
        return this;
    }

    @Override
    public void start() {
        this.chunkedTransferEncoding = this.config.chunkedTransferEncoding == null ?
                server.getChunkedTransferEncoding() : this.config.chunkedTransferEncoding;
    }

    @Override
    public void reload() {
        this.start();
    }

    @Override
    public String toString() {
        return "Location{" +
                "protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                '}';
    }

    private int getPortFromProtocol() {
        return "http".equals(this.protocol) ? 80 : 443;
    }

    private static class Config {
        private ChunkedTransferEncoding chunkedTransferEncoding;
    }

    public static void main(String[] args) {
        Location location = new Location(null);
        System.out.println(location.setProxyPass("http://mzhong.cn"));
        System.out.println(location.setProxyPass("http://mzhong.cn/"));
        System.out.println(location.setProxyPass("http://mzhong.cn/23432"));
        System.out.println(location.setProxyPass("http://mzhong.cn:80"));
        System.out.println(location.setProxyPass("http://mzhong.cn:80/"));
        System.out.println(location.setProxyPass("http://mzhong.cn:80/123"));
    }
}
