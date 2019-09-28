package com.pcz.udp.client;

import com.pcz.udp.bean.ServerInfo;
import com.pcz.udp.constants.UDPConstants;
import com.pcz.udp.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author picongzhi
 */
public class UDPSearcher {
    private static final int LISTEN_PORT = UDPConstants.CLIENT_RESPONSE_PORT;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher started...");

        CountDownLatch receiveCountDownLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = createListener(receiveCountDownLatch);
            sendBroadcast();

            receiveCountDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("UDPSearcher finished...");
        if (listener != null) {
            List<ServerInfo> serverInfoList = listener.getServerAndClose();
            if (serverInfoList.size() > 0) {
                return serverInfoList.get(0);
            }
        }

        return null;
    }

    private static Listener createListener(CountDownLatch receiveCountDownLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen...");
        CountDownLatch startCountDownLatch = new CountDownLatch(1);

        Listener listener = new Listener(LISTEN_PORT, startCountDownLatch, receiveCountDownLatch);
        listener.start();

        startCountDownLatch.await();

        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher send broadcast start...");
        DatagramSocket datagramSocket = new DatagramSocket();

        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        byteBuffer.putShort((short) 1);
        byteBuffer.putInt(LISTEN_PORT);

        DatagramPacket datagramPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.position());
        datagramPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        datagramPacket.setPort(UDPConstants.SERVER_PORT);

        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    private static class Listener extends Thread {
        private final int port;
        private final CountDownLatch startCountDownLatch;
        private final CountDownLatch receiveCountDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLength = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        private Listener(int port, CountDownLatch startCountDownLatch, CountDownLatch receiveCountDownLatch) {
            super();
            this.port = port;
            this.startCountDownLatch = startCountDownLatch;
            this.receiveCountDownLatch = receiveCountDownLatch;
        }

        @Override
        public void run() {
            super.run();

            // 通知已经开始
            startCountDownLatch.countDown();
            try {
                datagramSocket = new DatagramSocket(port);
                DatagramPacket receiveDatagramPacket = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    datagramSocket.receive(receiveDatagramPacket);

                    String ip = receiveDatagramPacket.getAddress().getHostAddress();
                    int port = receiveDatagramPacket.getPort();
                    int length = receiveDatagramPacket.getLength();
                    byte[] data = receiveDatagramPacket.getData();

                    boolean isValid = length > minLength && ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("UDPSearcher receive from ip: " + ip +
                            " port: " + port +
                            " valid: " + isValid);

                    if (!isValid) {
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,
                            UDPConstants.HEADER.length,
                            length - UDPConstants.HEADER.length);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 && serverPort <= 0) {
                        System.out.println("UDPSearcher receive cmd: " + cmd + " serverPort: " + serverPort);
                        continue;
                    }

                    String sn = new String(buffer, minLength, length - minLength);
                    ServerInfo serverInfo = new ServerInfo(sn, serverPort, ip);
                    serverInfoList.add(serverInfo);

                    receiveCountDownLatch.countDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }

            System.out.println("UDPSearcher finished...");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                ;
                datagramSocket = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();

            return serverInfoList;
        }
    }
}
