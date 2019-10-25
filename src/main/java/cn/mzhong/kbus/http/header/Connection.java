package cn.mzhong.kbus.http.header;

public enum Connection {
    KEEP_ALIVE("keep-alive"),
    CLOSE("close");

    public String value;

    Connection(String value) {
        this.value = value;
    }

    public static Connection valueOfString(String value) {
        for (Connection connection : Connection.values()) {
            if (connection.value.equals(value)) {
                return connection;
            }
        }
        return null;
    }
}
