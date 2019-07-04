package com.feipulai.device.udp;

import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.udp.parse.BasketballParser;
import com.feipulai.device.udp.parse.MiddleRaceParser;
import com.feipulai.device.udp.parse.UDPParser;
import com.feipulai.device.udp.result.UDPResult;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created by zzs on  2019/5/28
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class UdpChannelInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private ChannelHandlerContext handlerContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        handlerContext = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.d("nettyudp", "channelInactive");
        handlerContext.close();
        channelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE && UdpClient.HEART_BEAT.length > 0) {
                // 空闲了，发个心跳吧
                ctx.writeAndFlush(UdpClient.HEART_BEAT);
            }
        }
    }

    public void send(Object o) {
        if (handlerContext == null) {
            Log.d("nettyudp", "handlerContext  null");
            channelInactive();
            return;
        }

        handlerContext.writeAndFlush(o).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Log.d("nettyudp", "operationComplete " + future.isSuccess());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        //todo MachineCode.machineCode
        Log.d("nettyudp", "channelRead0===>" + datagramPacket.content());
        if (MachineCode.machineCode < 0)
            return;
        UDPParser parser = null;
        switch (MachineCode.machineCode) {
            case ItemDefault.CODE_LQYQ:
            case ItemDefault.CODE_ZQYQ:
                parser = new BasketballParser();
                break;
            case ItemDefault.CODE_ZCP:
                parser = new MiddleRaceParser();
                break;
        }
        UDPResult result = null;
        if (parser != null) {
            result = parser.parse(datagramPacket.content().array());
            if (result != null) {
                receive(result);
            }
        }

    }

    public abstract void receive(UDPResult result);

    public abstract void channelInactive();
}