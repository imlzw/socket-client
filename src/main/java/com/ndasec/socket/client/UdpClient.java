package com.ndasec.socket.client;

import com.ndasec.socket.utils.NumericalUtil;
import com.ndasec.socket.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UdpClient implements SocketClient {

    static protected final Logger log = LoggerFactory.getLogger(UdpClient.class);
    static protected byte[] SLICEFLAG = "...".getBytes();
    private Map<String, DatagramSocket> datagramSocketCache = new HashMap(100);

    /**
     * 发包处理
     *  处理多线程并发发送数据
     * @param bytes
     * @param packetSize
     * @param address
     * @param port
     * @throws IOException
     */
    public void send(byte[] bytes, int packetSize, String address, int port) throws IOException {
        String cacheKey = address + ":" + port;
        DatagramSocket datagramSocket = datagramSocketCache.get(cacheKey);
        if (datagramSocket == null) {
            datagramSocket = new DatagramSocket();
            datagramSocketCache.put(cacheKey, datagramSocket);
        }
        InetAddress inetAddress = InetAddress.getByName(address);
        if (datagramSocket.isClosed()) {
            datagramSocket.connect(inetAddress, port);
        }
        if (bytes.length <= packetSize) {
            //单个完整包，直接发送
            DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
            datagramSocket.send(sendPacket);
        } else {
            int size = packetSize - 40; //39字节为包id与包序号预留，格式【...UUID@@##[data]...】 ...为分片标志，UUID为包id,@@为分片总数,##为分片序号
            int len = bytes.length / size + 1;
            // 进制转换，定长转换
            int destHex = 80;
            String hexString = NumericalUtil.int2OtherHex(len, destHex);
            if (hexString.length() > 2) {
                log.error("数据包总分片大小["+len+"]超出规定长度["+(destHex*destHex)+"],无法正常发送数据包！");
                return;
            }else{
                hexString = StringUtils.padString(hexString, 2, '0');
            }

            // 分片处理
            String preInfo = "..." + UUID.randomUUID().toString().replace("-", "")+hexString;
            for (int i = 0; i < len; i++) {
                int to = (i + 1) * size;
                if (to > bytes.length) {
                    to = bytes.length;
                }
                byte[] buf = Arrays.copyOfRange(bytes, i * size, to);
                byte[] insertBytes = (preInfo + StringUtils.padString( NumericalUtil.int2OtherHex(i, destHex), 2, '0')).getBytes();
                buf = ArrayUtils.addAll(insertBytes, buf);
                DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, inetAddress, port);
                // 发送消息
                datagramSocket.send(sendPacket);
            }
        }
//        datagramSocket.close();
    }

    @Override
    public void close() {
        for (String key : datagramSocketCache.keySet()) {
            try {
                DatagramSocket datagramSocket = datagramSocketCache.get(key);
                datagramSocket.close();
            } catch (Exception e) {

            }
        }
    }

}
