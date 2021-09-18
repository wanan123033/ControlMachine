package com.feipulai.exam.tcp;

import android.content.Context;
import android.util.Log;

import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;

import static com.feipulai.exam.tcp.TCPConst.EVENT;
import static com.feipulai.exam.tcp.TCPConst.FIELD;
import static com.feipulai.exam.tcp.TCPConst.GAME;
import static com.feipulai.exam.tcp.TCPConst.SCHEDULE;
import static com.feipulai.exam.tcp.TCPConst.SORT;
import static com.feipulai.exam.tcp.TCPConst.SPORTS;
import static com.feipulai.exam.tcp.TCPConst.TRACK;


/**
 * created by ww on 2019/7/25.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class TcpDownLoadUtil {

    private Context mContext;
    private String tcpIp;
    private String tcpPort;
    private CommonListener downLoadListener;

    public static final int DOWNLOAD_FLAG = 2;

    public TcpDownLoadUtil(Context context, String tcpIp, String tcpPort, CommonListener listener) {
        mContext = context;
        this.tcpIp = tcpIp;
        this.tcpPort = tcpPort;
        this.downLoadListener = listener;
    }

    public void getTcp(int type, String itemName) {
        ResultPackage rcPackage = new ResultPackage();
        rcPackage.m_strSort = "";
        switch (type) {
            case SCHEDULE:
                rcPackage.m_strPackType = "PFPSchedule";
                break;
            case GAME:
                rcPackage.m_strPackType = "PFPGame";
                break;
            case SORT:
                rcPackage.m_strPackType = "PFPSort";
                break;
            case EVENT:
                rcPackage.m_strPackType = "PFPEvent";
                break;
            case TRACK:
                rcPackage.m_strEvent = TestConfigs.sCurrentItem.getItemName();
//                rcPackage.m_nSex = -1;
                rcPackage.m_strPackType = "PFPTrack";
//                rcPackage.m_strPackType = "PFPSporter";
                break;
            case FIELD:
                // rcPackage.m_strEvent = "1000米";
                 rcPackage.m_nSex = -1;
                rcPackage.m_strPackType = "PFPField";
                break;
            case SPORTS:
                rcPackage.m_strPackType = "PFPSporter";//无项目分组分道等信息，只有考生信息
                break;
            default:
                break;
        }

        rcPackage.m_nEventType = String.valueOf(TCPConst.enumEvent.EventAllData.getIndex());
        String data = rcPackage.EncodePackage(new PackageHeadInfo(), false, TCPConst.enumCodeType.CodeGB2312.getIndex());
        Log.i("data---", data);
        new SocketClient().getTCPConnect(tcpIp, Integer.parseInt(tcpPort), data, type, new SocketClient.CallBackSocketTCP() {
            @Override
            public void Receive(String info) {
                switch (Integer.parseInt(info)) {
                    case SCHEDULE:
                        getTcp(EVENT, "");
                        break;
                    case EVENT:
                        getTcp(TRACK, null);
                        break;
                    case TRACK:
                        getTcp(FIELD, null);
                        break;
                    case FIELD:
                        downLoadListener.onCommonListener(DOWNLOAD_FLAG, "数据下载完成");
                        break;
                    case SPORTS:
                        downLoadListener.onCommonListener(DOWNLOAD_FLAG, "数据下载完成");
                        break;
                    default:
                        break;
                }
                Log.i("Receive", info);
            }

            @Override
            public void isConnect(boolean state) {
                if (!state) {
                    downLoadListener.onCommonListener(DOWNLOAD_FLAG, "连接服务器失败");
                }
                Log.i("isConnect", "" + state);
            }
        });
    }
}
