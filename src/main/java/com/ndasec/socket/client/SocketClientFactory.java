package com.ndasec.socket.client;

public class SocketClientFactory {

    public static SocketClient getClient(String protocol) {
        String toLowerCase = protocol.toLowerCase();
        if ("tcp".equals(toLowerCase)) {
            return new TcpClient();
        } else if("udp".equals(toLowerCase)){
            return new UdpClient();
        } else {
            return new UdpClient();
        }
    }
}
