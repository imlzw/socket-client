package com.ndasec.socket.process;

import com.ndasec.socket.ExpireableCache;
import com.ndasec.socket.KeyLock;
import com.ndasec.socket.utils.NumericalUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据包接收处理程序
 */
public class PacketReciveProcess {

    static protected final Logger log = LoggerFactory.getLogger(PacketReciveProcess.class);

    // 重组包缓存
    private ExpireableCache recombinPacketCache = new ExpireableCache(64);
    // 重组包id计数器
    private Map<String, AtomicInteger> recombinPacketCountCache = new HashMap(64);
    // 关键key的细粒度锁
    private KeyLock<String> lock = new KeyLock<String>();
    private int maxPacketSize = 1024;

    // 接收超时，默认为30秒
    private long packetSliceCacheTimout = 1000*30l;

    ThreadPoolExecutor threadPoolExecutor = null;

    private String charset = "UTF-8";

    private PacketProcessHandler packetProcessHandler = null;

    public PacketReciveProcess(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
        this.packetProcessHandler = new PrintPacketProcessHandler();
        this.threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }


    public PacketReciveProcess(int maxPacketSize, ThreadPoolExecutor threadPoolExecutor) {
        this.maxPacketSize = maxPacketSize;
        this.threadPoolExecutor = threadPoolExecutor;
        this.packetProcessHandler = new PrintPacketProcessHandler();
    }

    public PacketReciveProcess(int maxPacketSize, ThreadPoolExecutor threadPoolExecutor, PacketProcessHandler packetProcessHandler) {
        this.maxPacketSize = maxPacketSize;
        this.threadPoolExecutor = threadPoolExecutor;
        this.packetProcessHandler = packetProcessHandler;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public PacketProcessHandler getPacketProcessHandler() {
        return packetProcessHandler;
    }

    public void setPacketProcessHandler(PacketProcessHandler packetProcessHandler) {
        this.packetProcessHandler = packetProcessHandler;
    }

    public long getPacketSliceCacheTimout() {
        return packetSliceCacheTimout;
    }

    public void setPacketSliceCacheTimout(long packetSliceCacheTimout) {
        this.packetSliceCacheTimout = packetSliceCacheTimout;
    }

    /**
     * 包数据接收，重组分片
     *
     * 39字节为包id与包序号预留，格式【...UUID@@##[data]...】 ...为分片标志，UUID为包id,@@为分片总数,##为分片序号
     * @param datagramSocket
     */
    public void processPacket(DatagramSocket datagramSocket) {
        DatagramPacket packet = new DatagramPacket(new byte[maxPacketSize], maxPacketSize);
        try {
            datagramSocket.receive(packet);
            String remoteAddress = packet.getAddress().getHostAddress();
            int remotePort = packet.getPort();
            String cacheKey = remoteAddress + ":" + remotePort;
            byte[] data = packet.getData();
            String msg = new String(data, 0, packet.getLength(), getCharset());
            // 判断是否是完整的包
            if (msg.startsWith("...")) { //分片包
                //获取包id与分片序号
                String info = msg.substring(0, 39);
                String packetId = info.substring(3, 35);
                int srcHex = 80;
                int sliceTotal = Integer.parseInt(NumericalUtil.BaseConvert(info.substring(35, 37), srcHex,10));
                int sliceNum = Integer.parseInt(NumericalUtil.BaseConvert(info.substring(37, 39), srcHex,10));
                msg = msg.substring(39);
                String key = cacheKey + ":" + packetId;
                byte[][] packetArray = (byte[][])recombinPacketCache.getCache(key);
                AtomicInteger count = recombinPacketCountCache.get(key); // 计数
                if (packetArray == null) {
                    // 多线程同步操作处理
                    lock.lock(key);// 打开锁, 仅对单个key加锁
                    try {
                        packetArray = (byte[][])recombinPacketCache.getCache(key);
                        count = recombinPacketCountCache.get(key);
                        if (packetArray == null) { // 重组数据
                            packetArray = new byte[sliceTotal][];
                            recombinPacketCache.setCache(key, packetArray, packetSliceCacheTimout);
                            count = new AtomicInteger(sliceTotal);
                            recombinPacketCountCache.put(key, count);
                        }
                    }finally {
                        lock.unlock(key);// 释放锁
                    }
                }
                try {
                    packetArray[sliceNum] = Arrays.copyOfRange(packet.getData(), 39, packet.getLength());
                } catch (Exception e) {
                    throw e;
                }
                if(count.addAndGet(-1) == 0){//所有的包都已经接收到
                    // 完整
                    recombinPacketCache.deleteCache(key);
                    recombinPacketCountCache.remove(key);
                    //  解析msg,缓存日志数据，根据运行模式处理日志
                    byte[] allBytes = new byte[0];
                    for (byte[] bytes : packetArray) {
                        allBytes = ArrayUtils.addAll(allBytes, bytes);
                    }
                    handler(datagramSocket, packet, allBytes, this.charset);
                }
            } else {
                handler(datagramSocket, packet, packet.getData(), this.charset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理报数据
     * @param datagramSocket
     * @param packet
     * @param totalData
     * @param charset
     */
    private void handler(DatagramSocket datagramSocket, DatagramPacket packet, byte[] totalData, String charset) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    packetProcessHandler.handle(datagramSocket, packet, totalData, charset);
                } catch (UnsupportedEncodingException e) {
                    log.error("无法处理数据包（远程地址：" + packet.getSocketAddress().toString() + ",接入端口：" + datagramSocket.getLocalPort() + "），不支持的编码：" + charset);
                } catch (Exception e) {
                    log.error("无法处理数据包（远程地址：" + packet.getSocketAddress().toString() + ",接入端口：" + datagramSocket.getLocalPort() + "），出现异常!", e);
                }
            }
        });
    }

    /**
     * 清理过期缓存数据
     *
     * @return 返回被清理的key列表
     */
    public void cleanExpireCache() {
        List<String> cacheKeys = this.recombinPacketCache.deleteTimeOut();
        if (cacheKeys != null) {
            for (String cacheKey : cacheKeys) {
                recombinPacketCountCache.remove(cacheKey);
            }
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println("...bef27312cded4aa3bba97c3db6b783454300".getBytes().length);

    }

}
