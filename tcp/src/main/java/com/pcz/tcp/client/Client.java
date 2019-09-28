package com.pcz.tcp.client;

import com.pcz.udp.bean.ServerInfo;
import com.pcz.udp.client.UDPSearcher;

import java.io.IOException;

/**
 * @author picongzhi
 */
public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        System.out.println("Server info: " + serverInfo);

        if (serverInfo != null) {
            try {
                TCPClient.linkWith(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
