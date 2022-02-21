package com.feipulai.exam.tcp;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentFace;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ZipUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import static com.feipulai.exam.tcp.PackageHeadInfo.g_SepFlag;
import static com.feipulai.exam.tcp.PackageHeadInfo.g_SepFlag1;
import static com.feipulai.exam.tcp.TCPConst.EVENT;
import static com.feipulai.exam.tcp.TCPConst.FIELD;
import static com.feipulai.exam.tcp.TCPConst.FPPICCARD;
import static com.feipulai.exam.tcp.TCPConst.GAME;
import static com.feipulai.exam.tcp.TCPConst.PHOTO;
import static com.feipulai.exam.tcp.TCPConst.SCHEDULE;
import static com.feipulai.exam.tcp.TCPConst.SENDCHECK;
import static com.feipulai.exam.tcp.TCPConst.SORT;
import static com.feipulai.exam.tcp.TCPConst.SPORTS;
import static com.feipulai.exam.tcp.TCPConst.TRACK;
import static org.greenrobot.eventbus.EventBus.TAG;

//{id=null, studentCode='1826710094', itemCode='default', machineCode=0, studentType=0, examType=0, scheduleNo='6', remark1='null', remark2='null', remark3='null'}
public class SocketClient {
    private Context mContext;
    private Socket socketTcp;
    private boolean flag;

    private String m_nEventType;
    private int studentNo;

    private int returnFlag = 0;

    private int UseLen;
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ArrayList<Item> itemInfos = new ArrayList<>();

    private CallBackSocketTCP call;



    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public interface CallBackSocketTCP {
        public void Receive(String info);

        public void isConnect(boolean state);
    }

    public SocketClient() {
        mContext = MyApplication.getInstance();
        UseLen = 0;
    }

    public void getTCPConnect(final String tcpIp, final int tcpPort, final String data, final int type, final CallBackSocketTCP call) {
        this.call = call;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socketTcp = new Socket();
                    socketTcp.connect(new InetSocketAddress(tcpIp, tcpPort), 12000);
                    socketTcp.setSoTimeout(8000);
                    sendData(data);
                    flag = true;
                    while (flag) {
                        boolean ba = isConnect();
                        //把值传给接口，这里接口作用就是传值
                        call.isConnect(ba);
//                        if (type == SCHEDULE){
//                            int info = receiverSchedule(type);
//                            call.Receive(String.valueOf(info));
//                            break;
//                        }
                        Log.e("TAG", "--------------------");
                        if (SENDCHECK == type) {
                            String info = receiveData2();
                            call.Receive(info);
                        } else if (FPPICCARD == type) {
                            flag = false;
                            call.Receive("");
                        } else if (PHOTO == type) {
                            InputStream inputStream = socketTcp.getInputStream();
                            receiveData(type, inputStream);
                        } else {
                            InputStream inputStream = socketTcp.getInputStream();
                            int info = receiveData(type, inputStream);
                            call.Receive(String.valueOf(info));
                        }
//                        int info = receiveData(type);
//                        call.Receive(String.valueOf(info));
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    call.isConnect(false);
                }
            }
        }.start();
    }

    //检测是否连接 如果断开就重连
    public boolean isConnect() {
        if (socketTcp.isClosed()) {//检测是否关闭状态
            //TODO 这个地方检测数 是断开，在这写重连的方法。

            return false;
        }
        return true;

    }

    //发送数据
    public void sendData(String data) throws IOException {
        OutputStream outputStream = socketTcp.getOutputStream();
        outputStream.write(data.getBytes("GB2312"));
    }

    //接收数据
    public String receiveData2() throws IOException {
        InputStream inputStream = socketTcp.getInputStream();
        byte[] buf = new byte[1024 * 1024 * 4];
        int len = inputStream.read(buf);
        String text = new String(buf, 0, len, "GB2312");
        flag = false;
        return text;
    }

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

    public int receiverSchedule(int ntype) throws IOException {
        InputStream inputStream = socketTcp.getInputStream();
        while (flag) {
            int available = inputStream.available();
            byte[] array = new byte[available];
            inputStream.read(array);
            String str = new String(array, "GBK");
            Log.e("TAG====>", str);
            if (str.contains("PFPSchedule")) {  //过滤第一条消息

            }
        }
        return 0;
    }

    public int receiveData(int ntype, InputStream inputStream) throws IOException {
//        InputStream inputStream = socketTcp.getInputStream();
        while (flag) {
            //验证固定长度的包头
            byte[] head = new byte[20];
            inputStream.read(head);
            String strHead = new String(head, "GBK");
            if (!strHead.substring(1, strHead.length() - 1).equals("FPTCP-PACKAGE-HEAD")) {
                Log.e(TAG, "包头错误: " + strHead);
                return 0;
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
            Log.i(TAG, "datalen" + (datalen - 58 - 20));
            if (ntype == PHOTO && datalen == 78) {
                byte[] footer = new byte[20];
                inputStream.read(footer);
                String strfooter = new String(head, "GBK");
                Log.e("TAG", "包尾：" + strfooter);
                continue;
            }
            byte[] data = new byte[datalen - 58 - 20];

            //非常重要---等待输入流中的数据完整
            while (inputStream.available() < data.length) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            inputStream.read(data);
            String dateStr = new String(data, "GB2312");
            String[] result = dateStr.split(getTarget());
            Log.i(TAG, "result: " + Arrays.toString(result) + "--------size:" + result.length);

            switch (ntype) {
                case SCHEDULE:
                    JieXiSchedule(result);
                    break;
                case EVENT:
                    JieXiEvent(result);
                    break;
                case TRACK:
                    JieXiStudent(result);
                    break;
                case FIELD:
                    JieXiStudent(result);
                    break;
                case SENDCHECK:
                    JieXiSend(result);
                    break;
                case SPORTS:
                    JieXiStudent(result);
                    break;
                case PHOTO:
                    JieXiPhoto(result, inputStream);
                    break;
            }
            if (ntype == PHOTO) {
                return returnFlag;
            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            byte[] end = new byte[20];
            inputStream.read(end);
            String endStr = new String(end, "GB2312");
            Log.i(TAG, "endStr: " + endStr.substring(1, endStr.length() - 1));
            if (endStr.substring(1, endStr.length() - 1).equals("FPTCP-PACKAGE-TAIL")) {
                if (m_nEventType != null && m_nEventType.equals("3")) {
                    switch (ntype) {
                        case SCHEDULE:
//                            DBManager.getInstance().saveSchedules(schedules);
                            break;
                        case GAME:
                            break;
                        case SORT:
                            break;
                        case EVENT:
                            Log.i("itemInfos", itemInfos.toString());

                            DBManager.getInstance().insertItem(itemInfos);
                            break;
                        case TRACK:
//                             DBManager.getInstance().insertStudent(studentInfos);
//							Log.i("studentNames", studentNames.toString());
                            break;
                        case FIELD:
                            // DBManager.getInstance(mContext).insertStudent(studentInfos);
                            break;
                        case SENDCHECK:
                            Log.e("原包返回", "-------------");
                            break;
                    }
                    returnFlag = ntype;
                    closeSocket();
                    return ntype;
                } else {
                    Log.e(TAG, "继续读取--------" + flag);
                    continue;
                }
            } else {
                Log.e("包尾错误", "---------------");
                return 0;
            }
        }
        return returnFlag;
    }

    //[, , 0, 2, PFPPhoto, , 0, , 立定跳远, 14891662, 500, 0, F1_ITEM_11, 6, 1, , 0, , 0, 0, 0, , 0, null, null, null, null, null, null, null, null, null, null]
    private void JieXiPhoto(String[] result, InputStream inputStream) {
        try {
            byte[] end = new byte[20];
            inputStream.read(end);
            String endStr = new String(end, "GB2312");
            Log.i(TAG, "endStr: " + endStr.substring(1, endStr.length() - 1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("TAG", "文件流数据：" + Arrays.toString(result));
        int m_nLayer = Integer.parseInt(result[9]);   //压缩包文件长度
        int m_nAllProp = Integer.parseInt(result[13]);//总压缩包数
        int m_nAllNum = Integer.parseInt(result[14]);   //当前第几个包
        int m_nGrp = Integer.parseInt(result[10]);//一个压缩包的头像个数
        Log.e("TAG", "m_nLayer=" + m_nLayer + ",m_nAllProp=" + m_nAllProp + ",m_nAllNum=" + m_nAllNum + ",m_nGrp=" + m_nGrp);
        try {
            byte[] buffer = new byte[m_nLayer];
            int ireadlen = 0;
            while (ireadlen < m_nLayer) {
                int ilen = inputStream.read(buffer, ireadlen, m_nLayer - ireadlen);
                ireadlen += ilen;
            }
//            Log.e("TAG",i+"-----------");
            Date date = new Date();
            File file = new File(MyApplication.PATH_IMAGE + date.getTime() + ".zip");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            bufferedOutputStream.write(buffer);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            if (m_nAllNum != m_nAllProp) {
                String[] returnData = Arrays.copyOf(result, 20);
                String arrayString = Arrays.toString(returnData);
                call.Receive(arrayString.substring(1, arrayString.length() - 1));
//                byte[] head = new byte[2048];
//                inputStream.read(head);
//                String strHead = new String(head, "GBK");
//                Log.e("TAG","第二包："+strHead);
                ZipUtils.unzipFile(file, file.getParentFile());

                receiveData(PHOTO, inputStream);

            } else {
                flag = false;
                File topicFile = new File(MyApplication.PATH_IMAGE + "photodown/");
                File[] files = topicFile.listFiles();
                for (File topic : files) {
                    File destFile = new File(MyApplication.PATH_IMAGE, topic.getName());
                    if (!destFile.exists()) {
                        destFile.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(destFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                    FileInputStream fis = new FileInputStream(topic);
                    byte[] bytes = new byte[4096];
                    int length = 0;
                    while ((length = fis.read(bytes)) != -1) {
                        bos.write(bytes, 0, length);
                        bos.flush();
                    }
                    bos.close();
                    fis.close();
                    topic.deleteOnExit();

                }
                String[] returnData = Arrays.copyOf(result, 20);
                String arrayString = Arrays.toString(returnData);
                call.Receive(arrayString.substring(1, arrayString.length() - 1));
                FileUtil.delete(MyApplication.PATH_IMAGE + "photodown/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void JieXiSend(String[] result) {
        if (result.length > 1) {
            m_nEventType = result[3];
            Log.i(TAG, "m_nEventType: " + m_nEventType);

            String m_strPackType = result[4];
            Log.i("m_strPackType: ", m_strPackType);
        }
    }

    private void JieXiStudent(String[] result) {
        Log.i("SocketClient", "JieXiStudent" + Arrays.toString(result));
        if (result.length > 1) {
            String m_strClientName = result[0];
            Log.i("m_strClientName", m_strClientName);

            String m_strServerName = result[1];
            Log.i("m_strServerName", m_strServerName);

            int m_lGrpID = Integer.parseInt(result[2]);
            Log.i("m_lGrpID", "--->" + m_lGrpID);
            m_nEventType = result[3];
            Log.i(ContentValues.TAG, "m_nEventType: " + m_nEventType);

            String m_strPackType = result[4];//包名
            Log.i("m_strPackType", m_strPackType);

            String m_strGameEventName = result[5];//组别全称
            Log.i("m_strGameEventName", m_strGameEventName);

            int m_nSex = Integer.parseInt(result[6]);//性别
            Log.i("m_nSex", "--->" + m_nSex);

            String m_strSort = result[7];//组别
            Log.i("m_strSort", m_strSort);

            String m_strEvent = result[8];//项目
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

            int m_nCheck = Integer.parseInt(result[18]);//组检录状态
            Log.i("m_nCheck", "-->" + m_nCheck);

            int m_nAffirmFlag = Integer.parseInt(result[19]);//组确认状态
            Log.i("m_nAffirmFlag", "-->" + m_nAffirmFlag);
            //当前包内学生数目
            studentNo = Integer.parseInt(result[20]);//运动员数目
            Log.i("studentNo", "-->" + studentNo);

            String m_strExamSitePlace = result[21];//考试场地
            Log.i("m_strExamSitePlace", m_strExamSitePlace);

            int m_nGroupType = Integer.parseInt(result[22]);


            if (result.length > 22) {
                //纪录名称
//                ArrayList<String> m_strRecordRanks = new ArrayList<>();
                //纪录值
//                ArrayList<String> m_strRecordResults = new ArrayList<>();
                //名次
//                ArrayList<Integer> gradenos = new ArrayList<>();
                //道号
                ArrayList<Integer> roadnos = new ArrayList<>();
                //运动员编号(准考证号)
                ArrayList<String> studentCodes = new ArrayList<>();
                //运动员名称
                ArrayList<String> m_studentNames = new ArrayList<>();
                //运动员学校
                ArrayList<String> schools = new ArrayList<>();
                //成绩
//                ArrayList<String> m_strResults = new ArrayList<>();
                //备注（考试编号）
                ArrayList<String> m_strNotes = new ArrayList<>();
                //成绩状态（DNS，DNF，DQ等状态）--检录包为检录状态，成绩包为成绩状态
//                ArrayList<String> m_strSporterScoreChecks = new ArrayList<>();
                //判读时间
//                ArrayList<String> m_strJudgeTimes = new ArrayList<>();
                //性别
                ArrayList<Integer> m_nGenders = new ArrayList<>();
                //单位代码(身份证)
                ArrayList<String> m_strUnitCodes = new ArrayList<>();
                //班级
                ArrayList<String> m_strClassName = new ArrayList<>();
                //考生类别（正常，免考，择考）/////////////---存放检录状态：未到，正常
//                ArrayList<String> m_strStudentCategory = new ArrayList<>();
                //最好成绩得分
                ArrayList<String> m_strBestScore = new ArrayList<>();
                //考试状态-正常，缓考，补考
                ArrayList<Integer> m_nExamStatus = new ArrayList<>();

                for (int i = 23; i < result.length; i++) {
                    if (i < 28) {
//                        m_strRecordRanks.add(result[i]);
                    } else if (i < 33) {
//                        m_strRecordResults.add(result[i]);
                    } else if (i < 33 + studentNo) {
//                        gradenos.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 2) {
                        roadnos.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 3) {
                        studentCodes.add(result[i]);
                    } else if (i < 33 + studentNo * 4) {
                        m_studentNames.add(result[i]);
                    } else if (i < 33 + studentNo * 5) {
                        schools.add(result[i]);
                    } else if (i < 33 + studentNo * 6) {
//                        m_strResults.add(result[i]);
                    } else if (i < 33 + studentNo * 7) {
                        m_strNotes.add(result[i]);
                    } else if (i < 33 + studentNo * 8) {
//                        m_strSporterScoreChecks.add(result[i]);
                    } else if (i < 33 + studentNo * 9) {
//                        m_strJudgeTimes.add(result[i]);
                    } else if (i < 33 + studentNo * 10) {
                        m_nGenders.add(Integer.parseInt(result[i]));
                    } else if (i < 33 + studentNo * 11) {
                        m_strUnitCodes.add(result[i]);
                    } else if (i < 33 + studentNo * 12) {
                        m_strClassName.add(result[i]);
                    } else if (i < 33 + studentNo * 13) {
//                        m_strStudentCategory.add(result[i]);
                    } else if (i < 33 + studentNo * 14) {
                        m_strBestScore.add(result[i]);
                    } else if (i < 33 + studentNo * 15) {
                        Log.i("m_nExamStatus", "--->" + Integer.parseInt(result[i]));
                        m_nExamStatus.add(Integer.parseInt(result[i]));
                    }
                }

                StudentItem studentItem;
                Student studentInfo;
                List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                for (int i = 0; i < studentNo; i++) {
                    studentInfo = DBManager.getInstance().queryStudentByCode(studentCodes.get(i));
                    if (studentInfo == null) {
                        studentInfo = new Student();
                        studentInfo.setSex(m_nGenders.get(i));
                        studentInfo.setStudentName(m_studentNames.get(i));
                        studentInfo.setClassName(m_strClassName.get(i));
                        studentInfo.setSchoolName(schools.get(i));
//                        studentInfo.setSort(m_strSort);
//                        studentInfo.setRollCallState(0);
//                        studentInfo.setUploadState(0);
                        studentInfo.setStudentCode(studentCodes.get(i));
//                        studentInfo.setExamCode(m_strNotes.get(i));
                        studentInfo.setIdCardNo(studentCodes.get(i));
                        if (!m_strBestScore.isEmpty() && !TextUtils.isEmpty(m_strBestScore.get(i))) {
                            studentInfo.setFaceFeature(m_strBestScore.get(i));
                            StudentFace studentFace = new StudentFace();
                            studentFace.setStudentCode(studentCodes.get(i));
                            studentFace.setFaceFeature(m_strBestScore.get(i));
                            DBManager.getInstance().insertStudentFace(studentFace);
                            FaceRegisterInfo faceFeature = new FaceRegisterInfo(Base64.decode(m_strBestScore.get(i), Base64.DEFAULT), studentInfo.getStudentCode());
                            registerInfoList.add(faceFeature);

                        }
                        if (m_lGrpID != 0) {
                            GroupItem itemGroup = new GroupItem();
                            Group group = new Group();
                            itemGroup.setItemCode(TestConfigs.getCurrentItemCode());
                            group.setItemCode(TestConfigs.getCurrentItemCode());
                            if (m_strSort.contains("补")) {
                                itemGroup.setGroupType(1);
                                group.setGroupType(1);
                            } else {
                                itemGroup.setGroupType(0);
                                group.setGroupType(0);
                            }
                            itemGroup.setSortName(m_strSort);
                            group.setSortName(m_strSort);
                            itemGroup.setGroupType(m_nSex);
                            group.setGroupType(m_nSex);

                            itemGroup.setStudentCode(studentInfo.getStudentCode());
//                itemGroup.setItemGroupName(m_strGameEventName);
                            itemGroup.setGroupNo(m_nGrp);
                            group.setGroupNo(m_nGrp);
//                itemGroup.setExamPlaceName(m_strExamSitePlace);
                            itemGroup.setScheduleNo(m_nField + "");
                            itemGroup.setTrackNo(roadnos.get(i));
                            group.setScheduleNo(m_nField + "");
//                itemGroup.setSubItemCode("");
                            DBManager.getInstance().insertGroupItem(itemGroup);
                            DBManager.getInstance().insertGroup(group);
                        }

                        ItemSchedule itemSchedule = new ItemSchedule();
                        itemSchedule.setScheduleNo(m_nField + "");
                        itemSchedule.setItemCode(TestConfigs.getCurrentItemCode());
                        DBManager.getInstance().insertItemSchedule(itemSchedule);
                        Log.e("TAG", studentInfo.toString());
                        DBManager.getInstance().insertStudent(studentInfo);
                    } else {
                        if (!TextUtils.isEmpty(m_strBestScore.get(i)) && studentInfo.getFaceFeature() == null) {
                            studentInfo.setFaceFeature(m_strBestScore.get(i));
                            FaceRegisterInfo faceFeature = new FaceRegisterInfo(Base64.decode(m_strBestScore.get(i), Base64.DEFAULT), studentInfo.getStudentCode());
                            registerInfoList.add(faceFeature);
                            DBManager.getInstance().updateStudent(studentInfo);
                            StudentFace studentFace = new StudentFace();
                            studentFace.setStudentCode(studentCodes.get(i));
                            studentFace.setFaceFeature(m_strBestScore.get(i));
                            DBManager.getInstance().insertStudentFace(studentFace);
                        }
                    }

//                    if (tcpType == TRACK || tcpType == FIELD) {
//                        studentInfo.setExamCode(studentCodes.get(i));
//                        studentInfo.setIdCardNo(m_strNotes.get(i));
//                    } else {
//                        studentInfo.setExamCode(m_strNotes.get(i));
//                        studentInfo.setIdCardNo(m_strUnitCodes.get(i));
//                    }
//                    studentInfos.add(studentInfo);

                    if (m_lGrpID == 0) {
                        studentItem = new StudentItem();
                        studentItem.setStudentCode(studentInfo.getStudentCode());
//                    studentItem.setSex(m_nSex);
//                    studentItem.setS(m_studentNames.get(i));
//                    studentItem.setExamCode(m_strNotes.get(i));
//                    studentItem.setSort(m_strSort);
//                    studentItem.setTrackNo(roadnos.get(i));
//                    studentItem.(m_nGrp + "");
//                    studentItem.setRaceNo(m_nLayer);
//                    studentItem.setBeganTime(m_strBeginTime);
                        studentItem.setExamType(m_nExamStatus.get(i));
//                    studentItem.setItemName(m_strEvent);
                        Item item = DBManager.getInstance().queryItemByName(m_strEvent);
                        studentItem.setItemCode(item.getItemCode());
                        studentItem.setMachineCode(item.getMachineCode());
                        studentItem.setScheduleNo(m_nField + "");
//                    studentItem.setItemNameAll(m_strGameEventName);
//                    studentItem.setExamPlace(m_strExamSitePlace);
                        studentItem.setStudentCode(studentCodes.get(i));
//                    studentItem.setSubItemCode("");
                        Log.e("TAG", studentItem.toString());
                        if ("null".equals(studentItem.getItemCode()) || TextUtils.isEmpty(studentItem.getItemCode())) {

                        } else {
                            DBManager.getInstance().insertStudentItem(studentItem);
                        }
                    }

                }
                FaceServer.getInstance().addFaceList(registerInfoList);
            }
        }
    }

    private void JieXiEvent(String[] result) {
        Log.e("TAG", "JieXiEvent===>" + Arrays.toString(result));
        if (result.length > 1) {
            m_nEventType = result[3];
            Log.i(TAG, "m_nEventType: " + m_nEventType);

            String m_strPackType = result[4];
            Log.i("m_strPackType: ", m_strPackType);

            String m_strEvent = result[8];
            Log.i("项目: ", m_strEvent);

            int m_nProperty = Integer.parseInt(result[11]);
            Log.i("项目属性: ", "--->" + Arrays.toString(result));
            int m_nItemCode = Integer.parseInt(result[13]);
            if (m_nEventType.equals("2")) {
                Item itemInfo = new Item();
                itemInfo.setItemName(m_strEvent);
                itemInfo.setMachineCode(m_nItemCode);
                itemInfo.setItemCode(result[12]);
                itemInfo.setTestType(m_nProperty);
                itemInfos.add(itemInfo);

            }
        }
    }

    private void JieXiSchedule(String[] result) {
        if (result.length > 1) {
            m_nEventType = result[3];
            Log.i(TAG, "m_nEventType: " + m_nEventType);

            String m_strPackType = result[4];
            Log.i("m_strPackType: ", m_strPackType);

            String m_strTime = result[8];
            Log.i("时间: ", m_strTime);

            int m_nScheduleNo = Integer.parseInt(result[20]);
            Log.i("日程编号: ", "--->" + m_nScheduleNo);

            if (m_nEventType.equals("2")) {
                Schedule schedule = new Schedule();
                schedule.setScheduleNo(String.valueOf(m_nScheduleNo));
                schedule.setBeginTime(DateUtil.getTimeMillis(m_strTime, "yyyy-MM-dd HH:mm:ss") + "");
                schedules.add(schedule);
                DBManager.getInstance().insertSchedule(schedule);
            }
        }
    }

    public void closeSocket() {
        if (socketTcp != null) {
            try {
                flag = false;
                socketTcp.shutdownInput();
                socketTcp.shutdownOutput();
                socketTcp.close();
//                socketTcp = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }
}
