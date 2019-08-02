package com.feipulai.exam.activity.MiddleDistanceRace.server;

/**
 * created by ww on 2019/7/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

import android.content.Context;
import android.util.Log;

import com.feipulai.common.tts.TtsConstants;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class MySocketServer {

    private boolean isEnable;
    private final WebConfig webConfig;//配置信息类
    private final ExecutorService threadPool;//线程池
    private ServerSocket socket;
    private OnResponseListener listener;
    private String m_strEvent;
    private Schedule schedule;
//    private Context mContext;

    public MySocketServer(WebConfig webConfig, OnResponseListener listener) {
        this.webConfig = webConfig;
        threadPool = Executors.newCachedThreadPool();
        this.listener = listener;
//        mContext=context;
    }

    /**
     * 开启server
     */
    public void startServerAsync() {
        isEnable = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doProcSync();
            }
        }).start();
    }

    /**
     * 关闭server
     */
    public void stopServerAsync() throws IOException {
        if (!isEnable) {
            return;
        }
        isEnable = true;
        socket.close();
        socket = null;
    }

    private void doProcSync() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(webConfig.getPort());
            socket = new ServerSocket();
            socket.bind(socketAddress);
            listener.OnTcpServerSuccess(true,"服务器开启成功");
            while (isEnable) {
                final Socket remotePeer = socket.accept();
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("tcp", "remotePeer..............." + remotePeer.getRemoteSocketAddress().toString());
                        listener.OnTcpServerSuccess(true,remotePeer.getRemoteSocketAddress().toString()+"连上本机");
                        onAcceptRemotePeer(remotePeer);
                    }
                });
            }
        } catch (IOException e) {
            listener.OnTcpServerSuccess(false,"服务器开启失败-" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onAcceptRemotePeer(Socket remotePeer) {
        try {
            remotePeer.getOutputStream().write("connected successful".getBytes());//告诉客户端连接成功
            // 从Socket当中得到InputStream对象
            InputStream inputStream = remotePeer.getInputStream();
            receiveData(inputStream);

//            byte buffer[] = new byte[1024 * 4];
//            int temp = 0;
//            // 从InputStream当中读取客户端所发送的数据
//            while ((temp = inputStream.read(buffer)) != -1) {
//                String info = new String(buffer, 0, temp, "GB2312");
//
////                String head = info.substring(0, 20);
////                String length = info.substring(20, 30);
////                String commond1 = info.substring(30, 36);
////                String key = info.substring(36, 46);
////                String codeStyle = info.substring(46, 47);
////                String isKey = info.substring(47, 48);
////                String time = info.substring(48, 58);
////
////                String data = info.substring(58, Integer.parseInt(length) - 20);
//
////                String[] result = data.split(getTarget());
////                JieXiStudent(result);
//                Log.e("tcp", info);
//                listener.onTcpReceiveResult(info);
//                remotePeer.getOutputStream().write(buffer, 0, temp);//把客户端传来的消息发送回去
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final byte[] g_SepFlag1 = {1};
    public static final byte[] g_SepFlag = {2};

    public String getTarget() {
        String target = null;
        try {
            String target1 = new String(g_SepFlag1, "GB2312");
            String target2 = new String(g_SepFlag, "GB2312");
            target = target1 + target2;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return target;
    }

    //接收数据
    public void receiveData(InputStream inputStream) throws IOException {
        //验证固定长度的包头
        byte[] head = new byte[20];
        inputStream.read(head);
        String strHead = new String(head, "GBK");
        if (!strHead.substring(1, strHead.length() - 1).equals("FPTCP-PACKAGE-HEAD")) {
            Log.e(TAG, "包头错误: " + strHead);
            return;
        }
        byte[] length = new byte[10];
        inputStream.read(length);
        String strLength = new String(length, "GB2312");
        int datalen = Integer.parseInt(strLength);
        Log.i(TAG, "datalen: " + datalen);

        byte[] commond1 = new byte[6];
        inputStream.read(commond1);
        int commondint = Integer.parseInt(new String(commond1, "GB2312"));

        byte[] key = new byte[10];
        inputStream.read(key);
        int keyint = Integer.parseInt(new String(key, "GB2312"));
        Log.i(TAG, "keyint" + keyint);

        byte[] type = new byte[1];
        inputStream.read(type);
        int typeint = Integer.parseInt(new String(type, "GB2312"));
        Log.i(TAG, "typeint" + typeint);

        byte[] iskey = new byte[1];
        inputStream.read(iskey);
        int iskeyint = Integer.parseInt(new String(iskey, "GB2312"));
        Log.i(TAG, "iskeyint" + iskeyint);

        byte[] time = new byte[10];
        inputStream.read(time);
        String timestr = new String(time, "GB2312");
        Log.i(TAG, "currentTimeMillis" + System.currentTimeMillis());
        Log.i(TAG, "timestr" + timestr);

        byte[] data = new byte[datalen - 58 - 20];
        //非常重要---等待输入流中的数据完整
        while (inputStream.available() < data.length) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        inputStream.read(data);
        String dateStr = new String(data, "GB2312");
        String[] result = dateStr.split(getTarget());
        Log.i(TAG, "result: " + result.toString() + "size:" + result.length);

        byte[] end = new byte[20];
        inputStream.read(end);
        String endStr = new String(end, "GB2312");
        Log.i(TAG, "endStr: " + endStr.substring(1, endStr.length() - 1));

        JieXiStudent(result);

        if (endStr.substring(1, endStr.length() - 1).equals("FPTCP-PACKAGE-TAIL")) {
            // TODO: 2019/7/30 回包
            listener.onTcpReceiveResult( schedule, m_strEvent);
        } else {
            Log.e("包尾错误", "---------------");
            return;
        }
    }

    private void JieXiStudent(String[] result) {
        if (result.length > 1) {
            String m_strClientName = result[0];
            Log.i("m_strClientName", m_strClientName);

            String m_strServerName = result[1];
            Log.i("m_strServerName", m_strServerName);

            int m_lGrpID = Integer.parseInt(result[2]);
            Log.i("m_lGrpID", "--->" + m_lGrpID);
            String m_nEventType = result[3];
            Log.i(TAG, "m_nEventType: " + m_nEventType);

            String m_strPackType = result[4];//包名
            Log.i("m_strPackType", m_strPackType);

            String m_strGameEventName = result[5];//项目全称
            Log.i("m_strGameEventName", m_strGameEventName);

            int m_nSex = Integer.parseInt(result[6]);//性别
            Log.i("m_nSex", "--->" + m_nSex);

            String m_strSort = result[7];//组别
            Log.i("m_strSort", m_strSort);

            m_strEvent = result[8];//项目
            Log.i("m_strEvent", m_strEvent);

            int m_nLayer = Integer.parseInt(result[9]);//赛次
            Log.i("m_nLayer", "--->" + m_nLayer);

            int m_nGrp = Integer.parseInt(result[10]);//分组
            Log.i("m_nGrp", "--->" + m_nGrp);

            int m_nProperty = Integer.parseInt(result[11]);//项目属性
            Log.i("m_nProperty", "--->" + m_nProperty);

            String m_strAllEventName = result[12];//全能分项名称
            Log.i("m_strAllEventName", m_strAllEventName);

            int m_nAllProp = Integer.parseInt(result[13]);//全能分项属性
            Log.i("m_nAllProp", "--->" + m_nAllProp);

            int m_nAllNum = Integer.parseInt(result[14]);//全能分项序号
            Log.i("m_nAllNum", "--->" + m_nAllNum);

            String m_strWindSpeed = result[15];//风向
            Log.i("m_strWindSpeed", m_strWindSpeed);

            int m_nField = Integer.parseInt(result[16]);//场次
            Log.i("m_nField", "-->" + m_nField);

            String m_strBeginTime = result[17];//分组比赛时间
            Log.i("m_strBeginTime", m_strBeginTime);

            schedule = DBManager.getInstance().getSchedulesByNo(m_nField + "");
            if (schedule == null) {
                schedule = DBManager.getInstance().insertSchedule(new Schedule(m_nField + "", m_strBeginTime, ""));
            }

            Item item = DBManager.getInstance().queryItemByName(m_strEvent);

            String itemCode;
            if (item == null) {
                itemCode = m_strEvent;//暂时用项目名代替
                DBManager.getInstance().insertItem(TestConfigs.sCurrentItem.getMachineCode(), itemCode, m_strEvent, "分'秒");

                ItemSchedule itemSchedule = new ItemSchedule();
                itemSchedule.setItemCode(itemCode);
                itemSchedule.setScheduleNo(m_nField + "");
                DBManager.getInstance().insertItemSchedule(itemSchedule);
            } else {
                itemCode = item.getItemCode();
            }


            int m_nCheck = Integer.parseInt(result[18]);//组检录状态
            Log.i("m_nCheck", "-->" + m_nCheck);

            int m_nAffirmFlag = Integer.parseInt(result[19]);//组确认状态
            Log.i("m_nAffirmFlag", "-->" + m_nAffirmFlag);
            //当前包内学生数目
            int studentNo = Integer.parseInt(result[20]);//运动员数目
            Log.i("studentNo", "-->" + result[20]);

            String m_strExamSitePlace = result[21];//考试场地
            Log.i("m_strExamSitePlace", m_strExamSitePlace);

            int m_nGroupType = Integer.parseInt(result[22]);

            if (result.length > 22) {
                //纪录名称
                ArrayList<String> m_strRecordRanks = new ArrayList<>();
                //纪录值
                ArrayList<String> m_strRecordResults = new ArrayList<>();
                //名次
                ArrayList<Integer> gradenos = new ArrayList<>();
                //道号
                ArrayList<Integer> roadnos = new ArrayList<>();
                //运动员编号
                ArrayList<String> studentCodes = new ArrayList<>();
                //运动员名称
                ArrayList<String> m_studentNames = new ArrayList<>();
                //运动员学校
                ArrayList<String> schools = new ArrayList<>();
                //成绩
                ArrayList<String> m_strResults = new ArrayList<>();
                //备注
                ArrayList<String> m_strNotes = new ArrayList<>();
                //成绩状态（DNS，DNF，DQ等状态）--检录包为检录状态，成绩包为成绩状态
                ArrayList<String> m_strSporterScoreChecks = new ArrayList<>();
                //判读时间
                ArrayList<String> m_strJudgeTimes = new ArrayList<>();
                //性别
                ArrayList<Integer> m_nGenders = new ArrayList<>();
                //单位代码
                ArrayList<String> m_strUnitCodes = new ArrayList<>();
                //班级
                ArrayList<String> m_strClassName = new ArrayList<>();
                //考生类别（正常，免考，择考）/////////////---存放检录状态：未到，正常
                ArrayList<String> m_strStudentCategory = new ArrayList<>();
                //最好成绩得分
                ArrayList<String> m_strBestScore = new ArrayList<>();
                //考试状态-正常，缓考，补考
                ArrayList<Integer> m_nExamStatus = new ArrayList<>();

                long currentTime = System.currentTimeMillis();
                for (int i = 23; i < result.length; i++) {
                    if (i < 28) {
                        m_strRecordRanks.add(result[i]);
                    } else if (i < 33) {
                        m_strRecordResults.add(result[i]);
                    } else if (i < 33 + studentNo) {
                        gradenos.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 2) {
                        roadnos.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 3) {
                        studentCodes.add(result[i]);
                    } else if (i < 33 + studentNo * 4) {
                        m_studentNames.add(result[i]);
                    } else if (i < 33 + studentNo * 5) {
                        schools.add(result[i]);
                    } else if (i < 33 + studentNo * 6) {
                        m_strResults.add(result[i]);
                    } else if (i < 33 + studentNo * 7) {
                        m_strNotes.add(result[i]);
                    } else if (i < 33 + studentNo * 8) {
                        m_strSporterScoreChecks.add(result[i]);
                    } else if (i < 33 + studentNo * 9) {
                        m_strJudgeTimes.add(result[i]);
                    } else if (i < 33 + studentNo * 10) {
                        m_nGenders.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 11) {
                        m_strUnitCodes.add(result[i]);
                    } else if (i < 33 + studentNo * 12) {
                        m_strClassName.add(result[i]);
                    } else if (i < 33 + studentNo * 13) {
                        m_strStudentCategory.add(result[i]);
                    } else if (i < 33 + studentNo * 14) {
                        m_strBestScore.add(result[i]);
                    } else if (i < 33 + studentNo * 15) {
                        m_nExamStatus.add(Integer.parseInt(result[i]));
                    }
                }

                for (int i = 0; i < studentNo; i++) {
                    Student student = new Student();
                    StudentItem studentItem = new StudentItem();

                    student.setSex(m_nGenders.get(i));
                    student.setStudentName(m_studentNames.get(i));
                    student.setClassName(m_strClassName.get(i));
                    student.setIdCardNo(m_strNotes.get(i));
                    student.setStudentCode(studentCodes.get(i));
                    student.setDownloadTime(currentTime + "");
                    student.setSchoolName(schools.get(i));
                    student.setSex(m_nGenders.get(i));


                    studentItem.setItemCode(itemCode);
                    studentItem.setScheduleNo(m_nField + "");
                    studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    studentItem.setStudentCode(studentCodes.get(i));
                    studentItem.setStudentType(m_nExamStatus.get(i));
                    studentItem.setExamType(m_nGroupType);

                    DBManager.getInstance().insertStudent(student);
                    DBManager.getInstance().insertStudentItem(studentItem);
                    Log.i(TAG,student.toString());

                    GroupItem groupItem = new GroupItem();
                    groupItem.setGroupNo(m_nGrp);
                    groupItem.setGroupType(m_nGroupType);
                    groupItem.setItemCode(itemCode);
                    groupItem.setScheduleNo(m_nField + "");
                    groupItem.setSortName(m_strSort);
                    groupItem.setStudentCode(studentCodes.get(i));
                    groupItem.setTrackNo(roadnos.get(i));
                    DBManager.getInstance().insertGroupItem(groupItem);
                }
            }
        }
    }

}