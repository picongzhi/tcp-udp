package com.pcz.tcp.server;

import com.pcz.udp.constants.TCPConstants;
import com.pcz.udp.server.UDPProvider;

import java.io.IOException;

/**
 * @author picongzhi
 */
public class Server {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer(TCPConstants.SERVER_PORT);
        boolean succeed = tcpServer.start();
        if (!succeed) {
            System.out.println("TCP Server start failed...");
            return;
        }

        UDPProvider.start(TCPConstants.SERVER_PORT);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UDPProvider.stop();
        tcpServer.stop();
    }
}
