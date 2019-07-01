package com.feipulai.device.tcp;

/**
 * created by ww on 2019/6/12.
 */
public interface NettyListener {
    byte STATUS_CONNECT_SUCCESS = 1;//连接成功

    byte STATUS_CONNECT_CLOSED = 0;//关闭连接

    byte STATUS_CONNECT_ERROR = 0;//连接失败


    /**
     * 当接收到系统消息
     */
    void onMessageResponse(Object msg);


    /**
     * 接收芯片信息
     * @param time 当前时间戳
     * @param cardId 当前接收的所有芯片ID
     */
    void onMessageReceive(long time, String []cardId);

    /**
     * 连接设备成功
     * @param text
     */
    void onConnected(String text);

    /**
     * 解析内容失败
     * @param msg
     */
    void onMessageFailed(Object msg);

    /**
     * 触发包（设备开始计时）
     * @param time 当前触发时间
     */
    void onStartTiming(long time);

    /**
     * 当连接状态发生变化时调用
     */
    void onServiceStatusConnectChanged(int statusCode);
}
