package com.feipulai.device.udp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.udp.result.UDPResult;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by zzs on  2019/5/28
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UdpClient extends UdpChannelInboundHandler implements Runnable {
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private volatile UdpChannelInitializer udpChannelInitializer;
    private int post;
    private String hostIp;
    private int inetPort;
    public static byte[] HEART_BEAT = new byte[]{};
    private UDPChannelListerner listerner;
    private ExecutorService executorService;
    private static volatile UdpClient udpClient;

//    public UdpClient(String hostIp, int post, int locatPost, UDPChannelListerner listerner) {
//        this.hostIp = hostIp;
//        this.post = post;
//        this.locatPost = locatPost;
//        this.listerner = listerner;
//        init();
//    }


    public synchronized static UdpClient getInstance() {
        if (udpClient == null) {
            udpClient = new UdpClient();
        }
        return udpClient;
    }

    public void setHostIpPostLocatListener(String hostIp, int post, UDPChannelListerner listerner) {
        this.hostIp = hostIp;
        this.post = post;

        this.listerner = listerner;
    }


    public void setHostIpPost(String hostIp, int post) {
        this.hostIp = hostIp;
        this.post = post;
    }

    public void init(int inetPort) {
        if (bootstrap != null) {
            return;
        }
        this.inetPort = inetPort;
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024);// 设置UDP写缓冲区为1M
        udpChannelInitializer = new UdpChannelInitializer(this);
        bootstrap.handler(udpChannelInitializer);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this);

    }

    @Override
    public void run() {
        Log.d("nettyudp", "run====>");
        try {
            ChannelFuture channelFuture = bootstrap.bind(inetPort).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            listerner.channelInactive();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    /**
     * 关闭
     */
    public void close() {
        bootstrap.remoteAddress(hostIp, post);
        udpChannelInitializer = null;
        bootstrap = null;
        udpClient = null;
        executorService.shutdownNow();
    }

    /**
     * 发送
     *
     * @param data
     */
    public void send(byte[] data) {
        Log.i("send---", Arrays.toString(data));
        send(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress(hostIp, post)));
    }

    @Override
    public void receive(UDPResult result) {
        Log.d("nettyudp", "receive====>" + result.toString());
        if (result != null) {
            Message msg = new Message();
            msg.obj = result;
            myHandle.sendMessage(msg);
        }

    }

    private Handler myHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (listerner != null)
                listerner.onDataArrived((UDPResult) msg.obj);
            return false;
        }
    });

    @Override
    public void channelInactive() {
        listerner.channelInactive();
    }

    public interface UDPChannelListerner {
        void channelInactive();

        void onDataArrived(UDPResult result);
    }
}

