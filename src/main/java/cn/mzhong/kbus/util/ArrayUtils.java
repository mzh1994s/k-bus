package cn.mzhong.kbus.util;

import java.util.Arrays;

/**
 * TODO<br>
 * 创建时间： 2019/10/25 17:23
 *
 * @author mzhong
 * @version 1.0
 */
public class ArrayUtils {
    private ArrayUtils() {
    }

    /**
     * 添加元素到数组中，返回一个新的数组
     *
     * @param arr
     * @param item
     * @param <T>
     * @return
     */
    public static <T> T[] add(T[] arr, T item) {
        int newLastIndex = arr.length;
        arr = Arrays.copyOf(arr, newLastIndex + 1);
        arr[newLastIndex] = item;
        return arr;
    }

    public static <T> T[] addAll(T[] arr, T[] items) {
        for (T item : items) {
            arr = add(arr, item);
        }
        return arr;
    }
}
