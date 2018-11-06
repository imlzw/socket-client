package com.ndasec.socket.process;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class PrintPacketProcessHandler implements PacketProcessHandler{

    static protected final Logger log = Logger.getLogger(PrintPacketProcessHandler.class.getSimpleName());

    public void handle(DatagramSocket datagramSocket, DatagramPacket packet, byte[] data, String charset) {
        try {
            log.info(new String(data,charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
