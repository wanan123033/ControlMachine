package com.feipulai.exam.tcp;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;

import org.greenrobot.eventbus.EventBus;

import static com.feipulai.exam.tcp.TCPConst.EVENT;
import static com.feipulai.exam.tcp.TCPConst.FIELD;
import static com.feipulai.exam.tcp.TCPConst.GAME;
import static com.feipulai.exam.tcp.TCPConst.PHOTO;
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

    /**
     * TCP下载   0  下载全部   1只下载项目日程
     * @param type
     * @param itemName
     * @param downType
     */
    public void getTcp(int type, String itemName, final int downType) {
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
                rcPackage.m_strEvent = itemName;
                rcPackage.m_nSex = -1;
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
            case PHOTO:
                rcPackage.m_strPackType = "PFPPhoto";
//                rcPackage.m_strEvent = itemName;
//                rcPackage.m_strAllEventName = TestConfigs.getCurrentItemCode();
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
                        getTcp(EVENT, "",downType);
                        break;
                    case EVENT:
                        int initState = TestConfigs.init(mContext, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                                    EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                                    ToastUtils.showShort("当前考点无此项目，请重新选择项目");
                                    downLoadListener.onCommonListener(DOWNLOAD_FLAG, "当前考点无此项目，请重新选择项目");
                                    return;
                                }else{
                                    if (downType == 0) {
                                        getTcp(TRACK, null, downType);
                                    }else {
                                        ToastUtils.showShort("下载完成");
                                        downLoadListener.onCommonListener(DOWNLOAD_FLAG, "下载完成");
                                    }
                                }

                            }
                        });
                        if (initState == TestConfigs.INIT_SUCCESS && !TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                            if (downType == 0) {
                                getTcp(TRACK, null, downType);
                            }else {
                                ToastUtils.showShort("下载完成");
                                downLoadListener.onCommonListener(DOWNLOAD_FLAG, "下载完成");
                            }
                        } else {
                            ToastUtils.showShort("当前考点无此项目，请重新选择项目");
                            downLoadListener.onCommonListener(DOWNLOAD_FLAG, "当前考点无此项目，请重新选择项目");
                        }
                        break;
                    case TRACK:
//                        getTcp(FIELD, null);
                        downLoadListener.onCommonListener(DOWNLOAD_FLAG, "数据下载完成");
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
