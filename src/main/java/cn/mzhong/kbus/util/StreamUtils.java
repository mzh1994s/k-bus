package cn.mzhong.kbus.util;

import java.io.*;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 16:40
 *
 * @author mzhong
 * @version 1.0
 */
public class StreamUtils {

    private StreamUtils() {
    }

    public static void closeSilent(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.flush();
    }

    public static void copyAt(InputStream inputStream, OutputStream outputStream, int length) throws IOException {
        if (length <= 0) {
            return;
        }
        int read, target = length;
        while (target > 0) {
            byte[] buf = new byte[target];
            if ((read = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, read);
                target -= read;
            } else {
                break;
            }
        }
    }

    public static byte[] read(InputStream inputStream, int length) throws IOException {
        if (length <= 0) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(length);
        copyAt(inputStream, outputStream, length);
        return outputStream.toByteArray();
    }

    /**
     * 读取一行，并且将原始数据写入outputStream
     *
     * @param inputStream  输入流
     * @param outputStream 输出数据流
     * @return 从inputStream中读取的一行数据，不包括\r\n
     * @throws IOException
     */
    public static String readLineAndWrite(InputStream inputStream, OutputStream outputStream) throws IOException {

        // 当第一次读取就是-1的话，证明输入流数据已经读取完毕，返回null
        int read = inputStream.read();
        if (read == -1) {
            return null;
        }
        outputStream.write(read);
        // 记录上一个字符，与read变量两个同事确定是否结尾是\r\n
        int previous = read;
        // 行数据流，每读一个字符就写一个字符，当遇到换行时终止，最终行数据流就是这一行的数据
        // 使用BufferedReader可以更好的实现此功能，但是为了保证性能，这里手动实现
        ByteArrayOutputStream lineStream = new ByteArrayOutputStream();
        // 写行流，只写出了换行符之外的数据
        if (read != '\r' && read != '\n') {
            lineStream.write(read);
        }

        // 上面是读取第一个字符，这里读取所有
        while ((read = inputStream.read()) != -1) {
            // 写数据流
            outputStream.write(read);
            // 写行流，不写换行符
            if (read != '\r' && read != '\n') {
                lineStream.write(read);
            }
            // 一行
            if (previous == '\r' && read == '\n') {
                break;
            }
            previous = read;
        }
        return new String(lineStream.toByteArray());
    }

    public static byte[] readLine(InputStream inputStream) throws IOException {

        // 当第一次读取就是-1的话，证明输入流数据已经读取完毕，返回null
        int read = inputStream.read();
        if (read == -1) {
            return null;
        }
        // 记录上一个字符，与read变量两个同事确定是否结尾是\r\n
        int previous = read;
        // 行数据流，每读一个字符就写一个字符，当遇到换行时终止，最终行数据流就是这一行的数据
        // 使用BufferedReader可以更好的实现此功能，但是为了保证性能，这里手动实现
        ByteArrayOutputStream lineStream = new ByteArrayOutputStream();
        // 写行流，只写出了换行符之外的数据
        if (read != '\r' && read != '\n') {
            lineStream.write(read);
        }

        // 上面是读取第一个字符，这里读取所有
        while ((read = inputStream.read()) != -1) {
            // 写行流，不写换行符
            if (read != '\r' && read != '\n') {
                lineStream.write(read);
            }
            // 一行
            if (previous == '\r' && read == '\n') {
                break;
            }
            previous = read;
        }
        lineStream.flush();
        return lineStream.toByteArray();
    }
}



