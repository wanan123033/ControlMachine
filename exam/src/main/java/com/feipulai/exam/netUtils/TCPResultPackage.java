package com.feipulai.exam.netUtils;

import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.tcp.PackageHeadInfo;
import com.feipulai.device.tcp.TCPConst;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.feipulai.device.tcp.PackageHeadInfo.g_SepFlag;
import static com.feipulai.device.tcp.PackageHeadInfo.g_SepFlag1;

/**
 * @author ww
 * @time 2020/5/6 13:36
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class TCPResultPackage {
    public String m_strClientName = "";
    //接收端（Server）名称
    public String m_strServerName = "";
    //分组ID需要和服务器一致(0表示个人，表示分组)
    public long m_lGrpID;
    //事件类型
    public String m_nEventType = "";
    //包类型---成绩包类型：SendRestultPackage .检录包类型：SendCheckPackage，数据获取包类型：GetStuInfoPackage
    public String m_strPackType = "";
    //项目全称如：男子甲组米决赛1 组
    public String m_strGameEventName = "";
    //性别，—代表男子，—代表女子
    public int m_nSex;
    //组别如：甲组
    public String m_strSort = "";

    //项目名称如：1000米
    public String m_strEvent = "";

    //赛次，-预赛，-次赛，-复赛，-决赛
    public int m_nLayer;
    //分组，如：
    public int m_nGrp;
    //项目属性1-径赛，-田高，-田远，-接力，-全能
    public int m_nProperty;
    //全能分项名称
    public String m_strAllEventName = "";
    //全能分项属性
    public int m_nAllProp;
    //全能分项序号
    public int m_nAllNum;
    //风速，如：+1.2，-0.21
    public String m_strWindSpeed = "";
    //场次
    public int m_nField;

    //分组比赛开始时间唯一标识，用于回包删除已发送包
    public String m_strBeginTime = "";

    //曾辉添加字段
    //分组ID需要和服务器一致（考试中的分组类型0-正常组，-缓考组，-补考组
    public int m_nGroupType;

    private List<RoundResultBean> examRoundResult = new ArrayList<>();
    private String m_strSpecialItemCode;
    private String m_strSpecialItemName;

    public String EncodePackage(Item item, List<UploadResults> uploadResults, PackageHeadInfo headInfo, boolean bEnCrypt, int type) {
        String target = null;
        try {
            String target1 = new String(g_SepFlag1, "GB2312");
            String target2 = new String(g_SepFlag, "GB2312");
            target = target1 + target2;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        String strBody;

        if (uploadResults == null) {
            strBody = "test";
        } else {
            Item resultItem = DBManager.getInstance().queryItemByCode(uploadResults.get(0).getExamItemCode());
            if (resultItem == null) {
                resultItem = TestConfigs.sCurrentItem;
            }
            //客户端
            sb.append(m_strClientName);
            sb.append(target);

            //服务端
            sb.append(m_strServerName);
            sb.append(target);

            //事件类型
            sb.append(m_nEventType);
            sb.append(target);

            //包类型
            sb.append(m_strPackType);
            sb.append(target);

            //项目全称
            sb.append(resultItem.getItemName());
            sb.append(target);
            if (!TextUtils.isEmpty(uploadResults.get(0).getGroupNo())) {
                //性别
                sb.append(uploadResults.get(0).getGroupType());
                sb.append(target);
                //组别
                sb.append(uploadResults.get(0).getSortName());
                sb.append(target);
            } else {
                //性别
                sb.append(m_nSex);
                sb.append(target);
                //组别
                sb.append(m_strSort);
                sb.append(target);
            }


            //项目名称
            sb.append(resultItem.getItemName());
            sb.append(target);

            //项目代码
            sb.append(resultItem.getItemCode());
            sb.append(target);

            //赛次
            sb.append(m_nLayer);
            sb.append(target);
            if (TextUtils.isEmpty(uploadResults.get(0).getGroupNo())) {
                //组号
                sb.append(m_nGrp);
                sb.append(target);
            } else {
                //组号
                sb.append(uploadResults.get(0).getGroupNo());
                sb.append(target);
            }

            Schedule schedule = DBManager.getInstance().getSchedulesByNo(uploadResults.get(0).getSiteScheduleNo());
            if (schedule == null) {
                //场次
                sb.append("");
                sb.append(target);
                //开始时间
                sb.append("");
                sb.append(target);
            } else {
                //场次
                sb.append(schedule.getScheduleNo());
                sb.append(target);
                //开始时间
                sb.append(schedule.getBeginTime());
                sb.append(target);
            }


            //考生人数
            sb.append(uploadResults.size());
            sb.append(target);

            //分组类型
            sb.append(m_nGroupType);
            sb.append(target);

            //项目属性
            sb.append(m_nProperty);
            sb.append(target);

            //全能分项名称
            sb.append(m_strAllEventName);
            sb.append(target);

            //全能分项属性
            sb.append(m_nAllProp);
            sb.append(target);

            //全能分项序号
            sb.append(m_nAllNum);
            sb.append(target);

            //风速
            sb.append(m_strWindSpeed);
            sb.append(target);

            //专项代码
            sb.append(m_strSpecialItemCode);
            sb.append(target);

            //专项名称
            sb.append(m_strSpecialItemName);
            sb.append(target);

            //备注1
            sb.append("");
            sb.append(target);

            //备注2
            sb.append("");
            sb.append(target);


            //备注3
            sb.append("");
            sb.append(target);


            //备注4
            sb.append("");
            sb.append(target);

            for (int i = 0; i < uploadResults.size(); i++)//m_vecExamRoundResult.size();
            {
                examRoundResult.clear();
                examRoundResult.addAll(uploadResults.get(i).getRoundResultList());

                //名次
                sb.append(0);//考试无
                sb.append(target);
                if (TextUtils.isEmpty(uploadResults.get(0).getGroupNo())) {
                    //道次
                    sb.append(0);//考试无
                    sb.append(target);
                } else {
                    Group group = DBManager.getInstance().queryGroupById(uploadResults.get(i).getGroupId());
                    GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, uploadResults.get(i).getStudentCode());
                    //道次
                    sb.append(groupItem.getTrackNo());//考试无
                    sb.append(target);
                }


                //考生考号
                sb.append(uploadResults.get(i).getStudentCode());
                sb.append(target);
                Student student = DBManager.getInstance().queryStudentByStuCode(uploadResults.get(i).getStudentCode());
                //考生姓名
                sb.append(student.getStudentName());
                sb.append(target);

                //性别
                sb.append(student.getSex());
                sb.append(target);

                //单位
                sb.append("");
                sb.append(target);

                //检录状态-0正常,1-缺考
                sb.append(0);
                sb.append(target);

                //考试状态-0正常，1缓考，2补考
                sb.append(uploadResults.get(i).getExamState());
                sb.append(target);

                //测试次数 (成绩数量)TODO 与中考确定传参是否是次数
                sb.append(examRoundResult.size());
                sb.append(target);


                //最好成绩
                sb.append(uploadResults.get(i).getResult());
                sb.append(target);

                //最好成绩得分
                sb.append("");
                sb.append(target);

                //成绩状态
                sb.append(uploadResults.get(i).getResultStatus());
                sb.append(target);

                //测试时间
                sb.append(DateUtil.formatTime1(Long.parseLong(uploadResults.get(i).getTestTime()), "yyyy-MM-dd HH:mm:ss.SSS"));
                sb.append(target);

                //备注1
                sb.append("");
                sb.append(target);

                //备注2
                sb.append("");
                sb.append(target);

                //备注3
                sb.append("");
                sb.append(target);

                //备注4
                sb.append("");
                sb.append(target);

                for (int j = 0; j < examRoundResult.size(); j++) {
                    //轮次
                    sb.append(examRoundResult.get(j).getRoundNo());
                    sb.append(target);

                    //成绩
                    sb.append(examRoundResult.get(j).getResult());
                    sb.append(target);

                    //状态
                    //成绩状态 TCP
                    sb.append(examRoundResult.get(j).getResultStatus());
                    sb.append(target);

                    //测试时间
                    sb.append(DateUtil.formatTime1(Long.parseLong(examRoundResult.get(j).getTestTime()), "yyyy-MM-dd HH:mm:ss.SSS"));
                    sb.append(target);

                    //是否为最终成绩
                    sb.append(examRoundResult.get(j).getResultType());
                    sb.append(target);

                    //备注1
                    sb.append(examRoundResult.get(j).getRemark1());
                    sb.append(target);

                    //备注2
                    sb.append(examRoundResult.get(j).getRemark2());
                    sb.append(target);

                    //备注3
                    sb.append(examRoundResult.get(j).getRemark3());
                    sb.append(target);
                }
            }
            strBody = sb.toString();
        }

        return SetEncodePackageBuffer(headInfo, strBody, type, bEnCrypt, TCPConst.enmuCommand.CommandStickResult, false);
    }

    private String SetEncodePackageBuffer(PackageHeadInfo headInfo, String strBody, int type, boolean bEnCrypt, TCPConst.enmuCommand nCommandID, boolean bBasePack) {
        String RS1 = "";
        String target = null;//特殊分割符的转换
        String target1 = null;
        try {
            target = new String(g_SepFlag, "GB2312");
            target1 = new String(g_SepFlag1, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int iTailLen = target1.getBytes().length + headInfo.g_FPTail.getBytes().length + target.getBytes().length;
        //打包加上系统时间
        if (type > 0) {
            if (!bBasePack)
                headInfo.m_dwTickCount = (int) (System.currentTimeMillis() / 1000);
            Log.i("time----------------> ", headInfo.m_dwTickCount + "");
        }
        Log.i("outBody-------------> ", strBody.getBytes().length + "");


        String strTickCount;
        //0017469927
        strTickCount = String.valueOf(headInfo.m_dwTickCount);
        if (type == 0)
            strBody = strTickCount;
        else
            strBody = strTickCount + strBody;
        int nBodyLen = 0;
        try {
            nBodyLen = strBody.getBytes("GB2312").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.i("nBodyLen---------> ", nBodyLen + "");
        if (bEnCrypt) {
            if (TCPConst.MAX_FPPACK_LEN < nBodyLen * 2)//只能使用明文
                headInfo.m_nEnCrypt = 0;
            else
                headInfo.m_nEnCrypt = 1;
        } else
            headInfo.m_nEnCrypt = 0;
        headInfo.m_nCommandID = nCommandID;

        Log.i("nCommandID---------> ", nCommandID + "");

        try {
            headInfo.m_nCryptLength = strBody.getBytes("GB2312").length;//将包体内容转换成字节流后再计算包体的长度
            Log.i("newBodyLen--------> ", strBody.trim().getBytes("GB2312").length + "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        headInfo.m_nTotalLength = TCPConst.HEAD_LEN + headInfo.m_nCryptLength + iTailLen;
        Log.i("iTailLen--------->: ", iTailLen + "");

        String strFPHead = target1 + headInfo.g_FPHead + target;//包头
        //   String strTotalLength = String.valueOf(headInfo.m_nTotalLength)+target2;//内容总长度
        String strTotalLength = String.format("%010d", headInfo.m_nTotalLength);
        String strCommandID = "";
        if (type == 0) {
            strCommandID = String.format("%06d", TCPConst.enumEvent.EventScore.getIndex());
        } else {
//            strCommandID = String.format("%06d", 2);
            strCommandID = String.format("%06d", nCommandID.getIndex());
        }

        Log.i("strCommandID---------> ", strCommandID + "");
        //  String strCryptLength = String.valueOf(headInfo.m_nCryptLength)+target2;//内容包的长度
        String strCryptLength = String.format("%010d", headInfo.m_nCryptLength);

        String strCodeType = String.valueOf(TCPConst.enumCodeType.CodeGB2312.getIndex());//默认编码
        String strEnCrypt = String.valueOf(headInfo.m_nEnCrypt);//加密内容
        String strFPTail = target1 + headInfo.g_FPTail + target;//包尾

        //回包组装
        if (type == 1 || type == 2)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strBody + strFPTail;
        else if (type == 3)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strBody + strFPTail;
        else if (type == 0)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strTickCount + strFPTail;
        int aa = strFPHead.getBytes().length;
        Log.i("headlength:-------> ", aa + "");

        return RS1;
    }

}
