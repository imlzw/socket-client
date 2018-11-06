package com.ndasec.socket.client;

import java.io.IOException;

public interface SocketClient {

   public void send(byte[] bytes, int packetSize, String address, int port) throws IOException;

   public void close();
}
