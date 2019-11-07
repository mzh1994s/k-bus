package cn.mzhong.kbus.util;

/**
 * TODO<br>
 * 创建时间： 2019/11/7 16:55
 *
 * @author mzhong
 * @version 1.0
 */
public class ByteUtils {
    private ByteUtils() {

    }

    /**
     * 将多个字节数组合并
     *
     * @param bytesArray
     * @return
     */
    public static byte[] merge(byte[]... bytesArray) {
        int length = 0;
        for (byte[] bytes : bytesArray) {
            length += bytes.length;
        }
        byte[] merged = new byte[length];
        int index = 0;
        for (byte[] bytes : bytesArray) {
            System.arraycopy(bytes, 0, merged, index, bytes.length);
            index += bytes.length;
        }
        return merged;
    }
}
