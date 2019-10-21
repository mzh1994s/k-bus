package cn.mzhong.kbus.http;

import java.io.InputStream;
import java.util.Map;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 17:32
 *
 * @author mzhong
 * @version 1.0
 */
class RequestInfo {
    Map<String, String> headers;
    InputStream inputStream;
}
