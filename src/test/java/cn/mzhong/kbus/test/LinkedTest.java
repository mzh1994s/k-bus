package cn.mzhong.kbus.test;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO<br>
 * 创建时间： 2019/11/6 17:13
 *
 * @author mzhong
 * @version 1.0
 */
public class LinkedTest {
    public static void main(String[] args) {
        List<Byte> listBytes = new LinkedList<>();
        ByteArrayOutputStream streamBytes = new ByteArrayOutputStream();
        int length = 10000000;
        listFill(listBytes, length);
        streamFill(streamBytes, length);

        listRead(listBytes);
        streamRead(streamBytes);
    }

    private static void listFill(List<Byte> listBytes, int length) {
        long start = System.currentTimeMillis();
        byte b = (byte) 1;
        for (int i = 0; i < length; i++) {
            listBytes.add(b);
        }
        System.out.println("listFill：" + (System.currentTimeMillis() - start));
    }

    private static void listRead(List<Byte> listBytes) {
        long start = System.currentTimeMillis();
        Iterator<Byte> iterator = listBytes.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        System.out.println("listRead：" + (System.currentTimeMillis() - start));
    }

    private static void streamFill(ByteArrayOutputStream streamBytes, int length) {
        long start = System.currentTimeMillis();
        byte b = (byte) 1;
        for (int i = 0; i < length; i++) {
            streamBytes.write(b);
        }
        System.out.println("streamFill：" + (System.currentTimeMillis() - start));
    }

    private static void streamRead(ByteArrayOutputStream streamBytes) {
        long start = System.currentTimeMillis();
        streamBytes.toByteArray();
        System.out.println("streamRead：" + (System.currentTimeMillis() - start));
    }
}
