package cn.mzhong.kbus.http;

/**
 * TODO<br>
 * 创建时间： 2019/10/24 15:30
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpLog {

    public static ThreadLocal<HttpLog> threadLocal = new ThreadLocal<>();

    private long start;
    /**
     * 请求行
     */
    private String requestLine;
    /**
     * 响应行
     */
    private String responseLine;

    /**
     * 建立连接耗时
     */
    private int connectExpand;
    /**
     * 请求耗时
     */
    private int requestExpand;
    /**
     * 响应耗时
     */
    private int responseExpand;

    public void start() {
        start = System.currentTimeMillis();
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public String getResponseLine() {
        return responseLine;
    }

    public void setResponseLine(String responseLine) {
        this.responseLine = responseLine;
    }

    public void saveConnectExpand() {
        connectExpand = (int) (System.currentTimeMillis() - start);
    }

    public void saveRequestExpand() {
        requestExpand = (int) (System.currentTimeMillis() - start);
    }

    public void saveResponseExpand() {
        responseExpand = (int) (System.currentTimeMillis() - start);
    }

    public int getConnectExpand() {
        return connectExpand;
    }

    public void setConnectExpand(int connectExpand) {
        this.connectExpand = connectExpand;
    }

    public int getRequestExpand() {
        return requestExpand;
    }

    public void setRequestExpand(int requestExpand) {
        this.requestExpand = requestExpand;
    }

    public int getResponseExpand() {
        return responseExpand;
    }

    public void setResponseExpand(int responseExpand) {
        this.responseExpand = responseExpand;
    }

    @Override
    public String toString() {
        return "HttpLog[" +
                "requestLine='" + requestLine + '\'' +
                ", responseLine='" + responseLine + '\'' +
                ", connectExpand=" + connectExpand +
                ", requestExpand=" + requestExpand +
                ", responseExpand=" + responseExpand +
                ']';
    }
}
