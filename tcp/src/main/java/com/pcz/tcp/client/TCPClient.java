package com.pcz.tcp.client;

import com.pcz.udp.bean.ServerInfo;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author picongzhi
 */
public class TCPClient {
    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getAddress()),
                serverInfo.getPort()), 3000);

        System.out.println("Client connected...");
        System.out.println("Client ip: " + socket.getLocalAddress() +
                " port: " + socket.getLocalPort());
        System.out.println("Server ip: " + socket.getInetAddress() +
                " port: " + socket.getPort());

        try {
            todo(socket);
        } catch (Exception e) {
            System.out.println(e);
        }

        socket.close();
        System.out.println("Client exit...");
    }

    private static void todo(Socket socket) throws IOException {
        BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream socketPrintStream = new PrintStream(socket.getOutputStream());

        boolean flag = true;
        do {
            String message = inputBufferedReader.readLine();
            socketPrintStream.println(message);

            String response = socketBufferedReader.readLine();
            if ("bye".equalsIgnoreCase(response)) {
                flag = false;
            } else {
                System.out.println(response);
            }
        } while (flag);

        inputBufferedReader.close();
        socketBufferedReader.close();
        socketPrintStream.close();
    }
}
