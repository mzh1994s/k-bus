package cn.mzhong.kbus.http.conf;

/**
 * 对应nginx的chunked_transfer_encoding项
 * <pre>
 * Syntax:	chunked_transfer_encoding on | off;
 * Default: chunked_transfer_encoding on;
 * Context:	http, server, location
 * </pre>
 * Allows disabling chunked transfer encoding in HTTP/1.1. It may come in handy when using a software failing to support
 * chunked encoding despite the standard’s requirement.<br>
 * 创建时间： 2019/10/25 11:06
 *
 * @author mzhong
 * @version 1.0
 */
public enum ChunkedTransferEncoding {
    /**
     * 允许将上游的Transfer-Encoding格式原封不动的传送给下游
     */
    ON,
    /**
     * 将上游的Transfer-Encoding格式转换为Content-Length格式
     */
    OFF
}
