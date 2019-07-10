package com.feipulai.device.tcp;

import android.util.Log;

import java.util.Arrays;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * created by ww on 2019/6/12.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "NettyClientHandler";
    private NettyListener listener;

    public NettyClientHandler(NettyListener listener) {
        this.listener = listener;
    }

    //每次给服务端发送的东西， 让服务端知道我们在连接中哎
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush("Heartbeat" + System.getProperty("line.separator"));
            }
        }
    }

    /**
     * 连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        super.channelActive(ctx);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "channelInactive");
    }

    private StringBuffer sb = new StringBuffer();

    //接收消息的地方，接口调用返回到activity了
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("客户端开始读取服务端过来的信息");
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String response = TcpConfig.bytesToHex(req);
        String[] response2 = TcpConfig.bytesToHexStrings(req);
        Log.i("onMessageResponse", "---" + response);

        if (response.equals(TcpConfig.CMD_CONNECT_RECEIVE)) {
            listener.onConnected("设备连接成功");
            return;
        }
        //解析收到的包
        if (response.startsWith("a1") && response.endsWith("fff8") && Integer.parseInt(response2[1] + response2[2], 16) == response2.length) {
            int[] timeByte = new int[7];
            long currentDate;
            //触发包标识 0xb0-触发包，0xb1-芯片包，0xb2-连接检查
            int flagNo = Integer.parseInt(response2[3], 16);
            if (flagNo == 176) {//b0
                //触发包时间和其它不一样
                timeByte[0] = Integer.parseInt(response2[5] + response2[6], 16);
                for (int i = 0; i < 5; i++) {
                    timeByte[i + 1] = Integer.parseInt(response2[7 + i], 16);
                }
                timeByte[6] = Integer.parseInt(response2[12] + response2[13], 16);
                currentDate = TcpConfig.getDateFromCMD(timeByte);
                listener.onStartTiming(currentDate);
                return;
            } else if (flagNo == 177) {//b1
                return;
            } else if (flagNo == 178) {//b2
                return;
            } else {
                timeByte[0] = Integer.parseInt(response2[4] + response2[5], 16);
                for (int i = 0; i < 5; i++) {
                    timeByte[i + 1] = Integer.parseInt(response2[6 + i], 16);
                }
                timeByte[6] = Integer.parseInt(response2[11] + response2[12], 16);
                currentDate = TcpConfig.getDateFromCMD(timeByte);

                sb.setLength(0);
                sb.append(response.substring(26, response.length() - 3));
                String[] cardIds = new String[flagNo];
                for (int i = 0; i < flagNo; i++) {
                    cardIds[i] = sb.substring(i * 24, i * 24 + 24);
                }
                Log.i("cardIds", Arrays.toString(cardIds));
                listener.onMessageReceive(currentDate, cardIds);
            }
        } else {
            listener.onMessageFailed("非法解析");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当引发异常时关闭连接。
        Log.e(TAG, "exceptionCaught");
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }

}
