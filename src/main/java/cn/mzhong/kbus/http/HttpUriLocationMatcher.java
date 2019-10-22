package cn.mzhong.kbus.http;

import java.util.Collection;

/**
 * TODO<br>
 * 创建时间： 2019/10/22 17:27
 *
 * @author mzhong
 * @version 1.0
 */
public class HttpUriLocationMatcher {

    public static Location match(Collection<Location> locations, String uri) {
        for (Location location : locations) {
            String value = location.getValue();
            if (uri.startsWith(value) || uri.matches(value)) {
                return location;
            }
        }
        return null;
    }
}
