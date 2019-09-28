package com.pcz.udp.client;

import com.pcz.udp.bean.ServerInfo;

/**
 * @author picongzhi
 */
public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        System.out.println("ServerInfo: " + serverInfo);
    }
}
