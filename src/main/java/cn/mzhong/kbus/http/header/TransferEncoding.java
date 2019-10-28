package cn.mzhong.kbus.http.header;

/**
 * TODO<br>
 * 创建时间： 2019/10/28 10:16
 *
 * @author mzhong
 * @version 1.0
 */
public enum TransferEncoding {
    /**
     * 数据以一系列分块的形式进行发送。 Content-Length 首部在这种情况下不被发送。。
     * 在每一个分块的开头需要添加当前分块的长度，以十六进制的形式表示，后面紧跟着 '\r\n' ，
     * 之后是分块本身，后面也是'\r\n' 。终止块是一个常规的分块，不同之处在于其长度为0。
     * 终止块后面是一个挂载（trailer），由一系列（或者为空）的实体消息首部构成。
     */
    CHUNKED,
    /**
     * 采用 Lempel-Ziv-Welch (LZW) 压缩算法。这个名称来自UNIX系统的 compress 程序，该程序实现了前述算法。
     * 与其同名程序已经在大部分UNIX发行版中消失一样，这种内容编码方式已经被大部分浏览器弃用，部分因为专利问题（这项专利在2003年到期）。
     */
    COMPRESS,
    /**
     * 采用 zlib 结构 (在 RFC 1950 中规定)，和 deflate 压缩算法(在 RFC 1951 中规定)。
     */
    DEFLATE,
    /**
     * 表示采用  Lempel-Ziv coding (LZ77) 压缩算法，以及32位CRC校验的编码方式。
     * 这个编码方式最初由 UNIX 平台上的 gzip 程序采用。处于兼容性的考虑，
     * HTTP/1.1 标准提议支持这种编码方式的服务器应该识别作为别名的 x-gzip 指令。
     */
    GZIP,
    /**
     * 用于指代自身（例如：未经过压缩和修改）。除非特别指明，这个标记始终可以被接受。
     */
    IDENTITY
}
