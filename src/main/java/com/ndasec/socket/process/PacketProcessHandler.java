package com.ndasec.socket.process;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 数据包处理器接口
 */
public interface PacketProcessHandler {

    void handle(DatagramSocket datagramSocket, DatagramPacket packet, byte[] totalData, String charset) throws UnsupportedEncodingException;

}
