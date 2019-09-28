package com.pcz.udp.constants;

/**
 * @author picongzhi
 */
public class UDPConstants {
    /**
     * 公共的头部
     */
    public static byte[] HEADER = new byte[]{7, 7, 7, 7, 7, 7, 7, 7};

    /**
     * 服务端UDP接收端口
     */
    public static int SERVER_PORT = 30201;

    /**
     * 客户端UDP端口
     */
    public static int CLIENT_RESPONSE_PORT = 30202;
}
