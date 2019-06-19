package com.feipulai.device.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by zzs on  2019/5/28
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UdpChannelInitializer extends ChannelInitializer<DatagramChannel> {
    private UdpChannelInboundHandler inboundHandler;

    public UdpChannelInitializer(UdpChannelInboundHandler handler) {
        inboundHandler = handler;
    }

    @Override
    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
        ChannelPipeline pipeline = datagramChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(12, 15, 0));
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast(inboundHandler);
    }
}
