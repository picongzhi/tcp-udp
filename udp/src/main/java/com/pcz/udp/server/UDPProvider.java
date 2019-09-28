package com.pcz.udp.server;

import com.pcz.udp.constants.UDPConstants;
import com.pcz.udp.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author picongzhi
 */
public class UDPProvider {
    private static Provider PROVIDER_INSTANCE;

    public static void start(int port) {
        stop();

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();

        PROVIDER_INSTANCE = provider;
    }

    public static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;
        private final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDP Provider started...");
            try {
                datagramSocket = new DatagramSocket(UDPConstants.SERVER_PORT);
                DatagramPacket receiveDatagramPacket = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    datagramSocket.receive(receiveDatagramPacket);

                    String clientIp = receiveDatagramPacket.getAddress().getHostAddress();
                    int clientPort = receiveDatagramPacket.getPort();
                    int clientDataLength = receiveDatagramPacket.getLength();
                    byte[] clientData = receiveDatagramPacket.getData();

                    boolean isValid = clientDataLength >= (UDPConstants.HEADER.length + 2 + 4) &&
                            ByteUtils.startsWith(clientData, UDPConstants.HEADER);
                    System.out.println("UDPProvider receive from ip: " + clientIp +
                            " port: " + clientPort +
                            " data valid: " + isValid);

                    if (!isValid) {
                        continue;
                    }

                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    if (cmd == 1 && responsePort > 0) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);

                        int length = byteBuffer.position();
                        DatagramPacket responseDatagramPocket = new DatagramPacket(buffer, length,
                                receiveDatagramPacket.getAddress(), responsePort);
                        datagramSocket.send(responseDatagramPocket);

                        System.out.println("UDPProvider response to: " + clientIp +
                                " port: " + responsePort +
                                " data length: " + length);
                    } else {
                        System.out.println("UDPProvider receive non support cmd: " + cmd +
                                " port: " + responsePort);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }

            System.out.println("UDP Provider finished...");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
