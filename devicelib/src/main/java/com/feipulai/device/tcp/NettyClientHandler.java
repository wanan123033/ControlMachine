package com.feipulai.device.tcp;

import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

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

    //每次给服务端发送的东西， 让服务端知道我们在连接中
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state() == IdleState.WRITER_IDLE) {
//                ctx.channel().writeAndFlush("Heartbeat" + System.getProperty("line.separator"));
//            }
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
    private String error1;
    private String[] error2;

    //接收消息的地方，接口调用返回到activity了
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];//此方法读取不完整
//        byte[] from = new byte[3];//先获取包长度
//        buf.readBytes(from);
//        String[] response1 = TcpConfig.bytesToHexStrings(from);
//
//        int len = Integer.parseInt(response1[2], 16);//包长
//
//        byte[] req = new byte[len - 3];//再获取包剩下的部分
        buf.readBytes(req);

        //合并两个byte数组得到完整包
//        byte[] byte_3 = new byte[from.length + req.length];
//        System.arraycopy(from, 0, byte_3, 0, from.length);
//        System.arraycopy(req, 0, byte_3, from.length, req.length);

        String response = TcpConfig.bytesToHex(req);
        String[] response2 = TcpConfig.bytesToHexStrings(req);

        if (response.equals(TcpConfig.CMD_CONNECT_RECEIVE)) {
            listener.onConnected("设备连接成功");
            return;
        }
        Logger.i("客户端开始读取服务端过来的信息:" + Arrays.toString(response2));
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
                listener.onMessageReceive(currentDate, cardIds);
            }
        } else {
            Logger.e("客户端开始读取服务端过来的信息:非法解析");
//            listener.onMessageFailed("非法解析");

            if (TextUtils.isEmpty(error1)) {
                error1 = response;
                error2 = response2.clone();
            } else {
                response = error1 + response;
                String [] byte_3 = new String [error2.length + response2.length];
                System.arraycopy(error2, 0, byte_3, 0, error2.length);
                System.arraycopy(response2, 0, byte_3, error2.length, response2.length);

                Logger.i("客户端开始读取服务端过来的信息(分包合并):" + Arrays.toString(byte_3));
                //解析收到的包
                if (response.startsWith("a1") && response.endsWith("fff8") && Integer.parseInt(byte_3[1] + byte_3[2], 16) == byte_3.length) {
                    int[] timeByte = new int[7];
                    long currentDate;
                    //触发包标识 0xb0-触发包，0xb1-芯片包，0xb2-连接检查
                    int flagNo = Integer.parseInt(byte_3[3], 16);
                    if (flagNo == 176) {//b0
                        //触发包时间和其它不一样
                        timeByte[0] = Integer.parseInt(byte_3[5] + byte_3[6], 16);
                        for (int i = 0; i < 5; i++) {
                            timeByte[i + 1] = Integer.parseInt(byte_3[7 + i], 16);
                        }
                        timeByte[6] = Integer.parseInt(byte_3[12] + byte_3[13], 16);
                        currentDate = TcpConfig.getDateFromCMD(timeByte);
                        listener.onStartTiming(currentDate);
                        return;
                    } else if (flagNo == 177) {//b1
                        return;
                    } else if (flagNo == 178) {//b2
                        return;
                    } else {
                        timeByte[0] = Integer.parseInt(byte_3[4] + byte_3[5], 16);
                        for (int i = 0; i < 5; i++) {
                            timeByte[i + 1] = Integer.parseInt(byte_3[6 + i], 16);
                        }
                        timeByte[6] = Integer.parseInt(byte_3[11] + byte_3[12], 16);
                        currentDate = TcpConfig.getDateFromCMD(timeByte);

                        sb.setLength(0);
                        sb.append(response.substring(26, response.length() - 3));
                        String[] cardIds = new String[flagNo];
                        for (int i = 0; i < flagNo; i++) {
                            cardIds[i] = sb.substring(i * 24, i * 24 + 24);
                        }
                        listener.onMessageReceive(currentDate, cardIds);
                    }
                }
                error1 = null;
                error2 = null;
            }

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
