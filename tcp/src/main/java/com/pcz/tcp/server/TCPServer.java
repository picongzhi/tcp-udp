package com.pcz.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author picongzhi
 */
public class TCPServer {
    private final int port;
    private ClientListener clientListener;

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            clientListener = new ClientListener(port);
            clientListener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void stop() {
        if (clientListener != null) {
            clientListener.exit();
        }
    }

    private static class ClientListener extends Thread {
        private ServerSocket serverSocket;
        private boolean done = false;

        private ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("Server ip: " + serverSocket.getInetAddress() +
                    " port: " + serverSocket.getLocalPort());
        }

        @Override
        public void run() {
            super.run();

            System.out.println("Server ready...");
            do {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } while (!done);

            System.out.println("Server closed...");
        }

        private void exit() {
            done = true;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private boolean done = false;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("Client connect, ip: " + socket.getInetAddress() + " port: " + socket.getPort());
            try {
                PrintStream socketPrintStream = new PrintStream(socket.getOutputStream());
                BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    String message = socketBufferedReader.readLine();
                    if ("bye".equalsIgnoreCase(message)) {
                        done = true;
                        socketPrintStream.println("bye");
                    } else {
                        System.out.println(message);
                        socketPrintStream.println("length: " + message.length());
                    }
                } while (!done);

                socketBufferedReader.close();
                socketPrintStream.close();
            } catch (IOException e) {
                System.out.println("Connection disconnected...");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Client exited, ip: " + socket.getInetAddress() +
                    " port: " + socket.getPort());
        }
    }
}
