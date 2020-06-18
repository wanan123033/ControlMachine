package com.feipulai.device.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * created by ww on 2020/5/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SendTcpClientThread extends Thread {

    private Socket socket = null;
    private OutputStream outputStream = null;        //输出流
    private InputStream inputStream = null;            //接收流
    private SendTcpListener listener;
    private int port;
    private String tcpIp;

    public interface SendTcpListener {
        /**
         * 获取服务器返回的数据
         *
         * @param text
         */
        void onMsgReceive(String text);

        /**
         * 发送数据失败
         *
         * @param msg
         */
        void onSendFail(String msg);

        /**
         * 服务器连接状态
         *
         * @param isConnect
         */
        void onConnectFlag(boolean isConnect);
    }

    public SendTcpClientThread(String ip, int port, SendTcpListener listener) {
        this.tcpIp = ip;
        this.port = port;
        this.listener = listener;
    }

    public volatile boolean exit = false;

    public void run() {
        try {
            socket = new Socket(tcpIp, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            listener.onConnectFlag(false);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectFlag(false);
            return;
        }


        //获取输出流
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectFlag(false);
            return;
        }

        listener.onConnectFlag(true);
        try {
            while (!exit)         //读取服务器端发送来的数据
            {
                byte[] buffer = new byte[1024];//创建接收缓冲区
                if (inputStream.available() > 0) {
                    int len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                    if (len > 0) {
                        //设置发送的内容
                        String text = new String(buffer, 0, len);
                        listener.onMsgReceive(text);
                    }
                }
            }
        } catch (IOException e) {

        }

    }

    /**
     * 向服务器端写入数据
     *
     * @param text
     * @return
     */
    public boolean write(String text) {
        boolean ret = true;
        try {
            outputStream.write(text.getBytes("GB2312"));
        } catch (IOException e) {
            ret = false;
            e.printStackTrace();
            listener.onSendFail(e.getMessage());
        }
        return ret;
    }
}
