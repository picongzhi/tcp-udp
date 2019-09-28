package com.pcz.udp.server;

import com.pcz.udp.constants.TCPConstants;

import java.io.IOException;

/**
 * @author picongzhi
 */
public class Server {
    public static void main(String[] args) {
        UDPProvider.start(TCPConstants.SERVER_PORT);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UDPProvider.stop();
    }
}
