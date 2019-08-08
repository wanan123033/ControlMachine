package com.feipulai.exam.activity.MiddleDistanceRace.server;

/**
 * created by ww on 2019/7/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
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
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class MySocketServer {

    private boolean isEnable;
    private final WebConfig webConfig;//配置信息类
    private final ExecutorService threadPool;//线程池
    private ServerSocket socket;
    public OnResponseListener listener;
    private String m_strEvent;
    private Schedule schedule;
    private byte[] dataByte;
    private InetSocketAddress socketAddress;

    public MySocketServer(WebConfig webConfig, OnResponseListener listener) {
        this.webConfig = webConfig;
        threadPool = Executors.newCachedThreadPool();
        this.listener = listener;
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
            socketAddress = new InetSocketAddress(webConfig.getPort());
            socket = new ServerSocket();
            socket.bind(socketAddress);
            if (listener != null) {
                listener.OnTcpServerSuccess(true, "起点上道服务器开启成功");
            }
            while (isEnable) {
                final Socket remotePeer = socket.accept();
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("tcp", "remotePeer..............." + remotePeer.getRemoteSocketAddress().toString());
                        Log.e("listener", "" + listener.toString());
                        if (listener != null) {
                            listener.OnTcpServerSuccess(true, remotePeer.getRemoteSocketAddress().toString() + "连上本机");
                        }
                        onAcceptRemotePeer(remotePeer);
                    }
                });
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.OnTcpServerSuccess(false, "服务器异常-" + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void onAcceptRemotePeer(Socket remotePeer) {
        try {
            // 从Socket当中得到InputStream对象
            InputStream inputStream = remotePeer.getInputStream();
            while (receiveData(inputStream)) {
                Log.i(TAG, new String(dataByte, 0, dataByte.length, "GB2312") + "---" + dataByte.length);
                remotePeer.getOutputStream().write(dataByte, 0, dataByte.length);//回客户端消息
                remotePeer.getOutputStream().flush();
            }
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
    public boolean receiveData(InputStream inputStream) throws IOException {
        //验证固定长度的包头
        byte[] head = new byte[20];
        inputStream.read(head);
        String strHead = new String(head, "GBK");
        if (!strHead.substring(1, strHead.length() - 1).equals("FPTCP-PACKAGE-HEAD")) {
            Log.e(TAG, "包头错误: " + strHead);
            return false;
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

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(strHead)
                .append("0000000078")
                .append("000001")
                .append("0000000010")
                .append(new String(type, "GB2312"))
                .append(new String(iskey, "GB2312"))
                .append(timestr)
                .append(endStr);
        dataByte = stringBuffer.toString().getBytes();

        JieXiStudent(result);

        if (endStr.substring(1, endStr.length() - 1).equals("FPTCP-PACKAGE-TAIL")) {
            Log.e(TAG, "-----------");
            Log.e("listener", "" + listener.toString());
            if (listener != null) {
                listener.onTcpReceiveResult(schedule, m_strEvent);
            }
//            remotePeer.getOutputStream().write(time, 0, time.length);//把客户端传来的消息发送回去
            return true;
        } else {
            Log.e("包尾错误", "---------------");
            return false;
        }
    }

    private void JieXiStudent(String[] result) {
        Log.i("result------------", Arrays.toString(result));
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
            m_strBeginTime = DateUtil.formatTime3(m_strBeginTime);

            int m_nCheck = Integer.parseInt(result[18]);//组检录状态
            Log.i("m_nCheck", "-->" + m_nCheck);

            int m_nAffirmFlag = Integer.parseInt(result[19]);//组确认状态
            Log.i("m_nAffirmFlag", "-->" + m_nAffirmFlag);
            //当前包内学生数目
            int studentNo = Integer.parseInt(result[20]);//运动员数目
            Log.i("studentNo", "-->" + result[20]);

            String m_strExamSitePlace = result[21];//考试场地
//            Log.i("m_strExamSitePlace", m_strExamSitePlace);

            int m_nGroupType = Integer.parseInt(result[22]);

            schedule = DBManager.getInstance().getSchedulesByNo(m_nField + "");
            if (schedule == null) {
                schedule = DBManager.getInstance().insertSchedule(new Schedule(m_nField + "", m_strBeginTime, ""));
            }

            Item item = DBManager.getInstance().queryItemByName(m_strEvent);

            String itemCode;
            if (item == null) {
                itemCode = "fpl_" + m_strEvent;//暂时用项目名代替
                DBManager.getInstance().insertItem(TestConfigs.sCurrentItem.getMachineCode(), itemCode, m_strEvent, "分'秒");
            } else {
                if (TextUtils.isEmpty(item.getItemCode())) {
                    itemCode = "fpl_" + m_strEvent;
                    item.setItemCode(itemCode);
                    DBManager.getInstance().updateItem(item);
                } else {
                    itemCode = item.getItemCode();
                }
            }


            ItemSchedule itemSchedule = new ItemSchedule();
            itemSchedule.setItemCode(itemCode);
            itemSchedule.setScheduleNo(m_nField + "");
            DBManager.getInstance().insertItemSchedule(itemSchedule);

            Group group = new Group();
            group.setGroupNo(m_nGrp);
            group.setGroupType(m_nGroupType);
            group.setItemCode(itemCode);
            group.setScheduleNo(schedule.getScheduleNo());
            group.setSortName(m_strSort);
            DBManager.getInstance().insertGroup(group);

            if (result.length > 23) {
                //纪录名称
//                ArrayList<String> m_strRecordRanks = new ArrayList<>();
//                //纪录值
//                ArrayList<String> m_strRecordResults = new ArrayList<>();
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
//                ArrayList<String> m_strResults = new ArrayList<>();
                //备注
//                ArrayList<String> m_strNotes = new ArrayList<>();
                //成绩状态（DNS，DNF，DQ等状态）--检录包为检录状态，成绩包为成绩状态
//                ArrayList<String> m_strSporterScoreChecks = new ArrayList<>();
                //判读时间
//                ArrayList<String> m_strJudgeTimes = new ArrayList<>();
                //性别
                ArrayList<Integer> m_nGenders = new ArrayList<>();
                //单位代码
//                ArrayList<String> m_strUnitCodes = new ArrayList<>();
                //班级
//                ArrayList<String> m_strClassName = new ArrayList<>();
                //考生类别（正常，免考，择考）/////////////---存放检录状态：未到，正常
                ArrayList<String> m_strStudentCategory = new ArrayList<>();
                //最好成绩得分
//                ArrayList<String> m_strBestScore = new ArrayList<>();
                //考试状态-正常，缓考，补考
                ArrayList<Integer> m_nExamStatus = new ArrayList<>();

                long currentTime = System.currentTimeMillis();

                Log.e("currentTime", "------------" + currentTime);

                for (int i = 0; i < studentNo; i++) {
                    gradenos.add(Integer.parseInt(result[i + 33]));
                    roadnos.add(Integer.parseInt(result[i + 33 + studentNo]));
                    studentCodes.add(result[i + 33 + studentNo * 2]);
                    m_studentNames.add(result[i + 33 + studentNo * 3]);
                    schools.add(result[i + 33 + studentNo * 4]);
                    m_nGenders.add(Integer.parseInt(result[i + 33 + studentNo * 9]));
                    m_strStudentCategory.add(result[i + 33 + studentNo * 12]);
                    m_nExamStatus.add(Integer.parseInt(result[i + 33 + studentNo * 14]));
                }

                Log.i(TAG, gradenos.toString());
                Log.i(TAG, roadnos.toString());
                Log.i(TAG, studentCodes.toString());
                Log.i(TAG, m_studentNames.toString());
                Log.i(TAG, schools.toString());
                Log.i(TAG, m_nGenders.toString());
                Log.i(TAG, m_strStudentCategory.toString());
                Log.i(TAG, m_nExamStatus.toString());

                Student student;
                StudentItem studentItem;
                for (int i = 0; i < studentNo; i++) {
                    Log.e(TAG, i + "---" + m_studentNames.get(i));

                    student = DBManager.getInstance().queryStudentByStuCode(studentCodes.get(i));
                    if (student == null) {
                        student = new Student();
                        studentItem = new StudentItem();

                        student.setSex(m_nGenders.get(i));
                        student.setStudentName(m_studentNames.get(i));
                        student.setStudentCode(studentCodes.get(i));
                        student.setDownloadTime(currentTime + "");
                        student.setSchoolName(schools.get(i));

                        studentItem.setItemCode(itemCode);
                        studentItem.setScheduleNo(m_nField + "");
                        studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                        studentItem.setStudentCode(studentCodes.get(i));
                        studentItem.setStudentType(m_nExamStatus.get(i));
                        studentItem.setExamType(m_nExamStatus.get(i));

                        DBManager.getInstance().insertStudent(student);
                        DBManager.getInstance().insertStudentItem(studentItem);
                    }

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