package cn.mzhong.kbus.proxy;

import java.net.Socket;

/**
 * TODO<br>
 * 创建时间： 2019/10/21 16:45
 *
 * @author mzhong
 * @version 1.0
 */
public class SocketProxy implements Proxy<Socket, Socket> {
    
    @Override
    public void proxy(Socket accept, Socket client) {

    }
}
