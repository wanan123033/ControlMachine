package com.feipulai.device.tcp;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import static com.feipulai.device.tcp.PackageHeadInfo.g_SepFlag;
import static com.feipulai.device.tcp.PackageHeadInfo.g_SepFlag1;
import static com.feipulai.device.tcp.TCPConst.MAX_RECORD_COUNT;
import static com.feipulai.device.tcp.TCPConst.MAX_RUN_SPORTER_NUM;

/**
 * Created by ww on 2018/1/10.
 */

public class ResultPackage {
   public String    m_strClientName="";
    //接收端（Server）名称
    public String    m_strServerName="";
    //分组ID需要和服务器一致(0表示个人，表示分组)
    public long       m_lGrpID;
    //事件类型
    public String   m_nEventType="";
    //包类型---成绩包类型：SendRestultPackage .检录包类型：SendCheckPackage，数据获取包类型：GetStuInfoPackage
    public String    m_strPackType="";
//项目全称如：男子甲组米决赛1 组
public String    m_strGameEventName="";
    //性别，—代表男子，—代表女子
    public int        m_nSex;
    //组别如：甲组
    public  String    m_strSort="";

    //项目名称如：1000米
    public String    m_strEvent="";

    //赛次，-预赛，-次赛，-复赛，-决赛
    public int		   m_nLayer;
    //分组，如：
    public int        m_nGrp;
    //项目属性1-径赛，-田高，-田远，-接力，-全能
    public int		   m_nProperty;
    //全能分项名称
    public String    m_strAllEventName="";
    //全能分项属性
    public int        m_nAllProp;
    //全能分项序号
    public int        m_nAllNum;
    //风速，如：+1.2，-0.21
    public String    m_strWindSpeed="";
    //场次
    public int        m_nField;

    //分组比赛开始时间唯一标识，用于回包删除已发送包
    public String    m_strBeginTime="";

    //检录状态
    public int        m_nCheck;
    //成绩是否确认
    public int        m_nAffirmFlag;

    //运动员数目
    public int        m_nSporterNum;

    //纪录名称
    public String[]    m_strRecordRank = new String[MAX_RECORD_COUNT];
    //纪录值
    public  String[]    m_strRecordResult = new String[MAX_RECORD_COUNT];
    //名次
    public int[]        m_nPlace = new int[MAX_RUN_SPORTER_NUM];
    //道号
    public  int[]        m_nTrackNO = new int[MAX_RUN_SPORTER_NUM];
    //运动员编号
    public  String[]    m_strSporterID = new String[MAX_RUN_SPORTER_NUM];
    //运动员姓名
    public String[]    m_strSporterName = new String[MAX_RUN_SPORTER_NUM];
    //单位名称
    public String []   m_strUnitName = new String[MAX_RUN_SPORTER_NUM];
    //成绩
    public String[]    m_strResult =  new String[MAX_RUN_SPORTER_NUM];
    //备注---轮次
    public  String[]    m_strNote = new String[MAX_RUN_SPORTER_NUM];
    //成绩状态（DNS，DNF，DQ等状态）--检录包为检录状态，成绩包为成绩状态
    public String[]    m_strSporterScoreCheck = new String[MAX_RUN_SPORTER_NUM];
    //判读时间
    public String[]    m_strJudgeTime = new String[MAX_RUN_SPORTER_NUM];
    //曾辉添加字段
//考试场地//////////////
    public  String	   m_strExamSitePlace="";
    //分组ID需要和服务器一致（考试中的分组类型0-正常组，-缓考组，-补考组
    public int        m_nGroupType;
    //性别
    public int[]		   m_nGender = new int[MAX_RUN_SPORTER_NUM];
    //单位代码
    public String[]    m_strUnitCode = new String[MAX_RUN_SPORTER_NUM];
    //班级
    public  String[]    m_strClassName = new String[MAX_RUN_SPORTER_NUM];
    //考生类别（正常，免考，择考）/////////////---存放检录状态：未到，正常
    public String[] m_strStudentCategory = new String[MAX_RUN_SPORTER_NUM];
    //最好成绩得分
    public String[]    m_strBestScore = new String[MAX_RUN_SPORTER_NUM];
    //考试状态-正常，缓考，补考
    public int	[]	   m_nExamStatus = new int[MAX_RUN_SPORTER_NUM];

    public String[] getM_strSporterID() {
        return m_strSporterID;
    }

    public void setM_strSporterID(String[] m_strSporterID) {
        this.m_strSporterID = m_strSporterID;
    }

    public String[] getM_strSporterName() {
        return m_strSporterName;
    }

    public void setM_strSporterName(String[] m_strSporterName) {
        this.m_strSporterName = m_strSporterName;
    }

    public int[] getM_nGender() {
        return m_nGender;
    }

    public void setM_nGender(int[] m_nGender) {
        this.m_nGender = m_nGender;
    }

    public String[] getM_strSporterScoreCheck() {
        return m_strSporterScoreCheck;
    }

    public void setM_strSporterScoreCheck(String[] m_strSporterScoreCheck) {
        this.m_strSporterScoreCheck = m_strSporterScoreCheck;
    }

    /**
     * 计算分包数目
     * @param SPCount
     * @return
     */
    public int GetPackageNum(int SPCount)
    {
        int PackageNum = 0;
        if (SPCount % MAX_RUN_SPORTER_NUM != 0)
            PackageNum = SPCount / MAX_RUN_SPORTER_NUM + 1;
        else
            PackageNum = SPCount / MAX_RUN_SPORTER_NUM;
        return PackageNum;
    }
//解包方法
    public int DecodePackage (String[] ssArray,ResultPackage rc){
        if (ssArray.length == 0)
            return -1;
        int nPos = 1;
        String strHeadInfo = ssArray[0].trim();
        Log.i( "strHeadInfo:-------->",strHeadInfo );



    //    String strHeadInfo = ssArray[0].trim();
      //  Log.e( "strHeadInfo:-------->",strHeadInfo );
     //   String strTime =  ssArray[1].trim();
      //  Log.e( "strTime:-------->",strTime );


//        nPos++;
//        String strHeadInfo = ssArray[nPos].trim();//包头内容
//        nPos++;
//        rc.m_strClientName = ssArray[nPos].trim();
//        nPos++;
//        rc.m_strServerName = ssArray[nPos].trim();//3
//        nPos+= 2;
//        rc.m_lGrpID = Long.parseLong(ssArray[nPos].trim());//5
//        nPos += 2;
//        rc.m_nEventType = Integer.parseInt(ssArray[nPos].trim())+"";//7
//        nPos += 2;
//        rc.m_strPackType = ssArray[nPos].trim();//9
//        nPos += 2;
//        rc.m_strGameEventName = ssArray[nPos].trim();//11
//        nPos += 2;
//        rc.m_nSex = Integer.parseInt(ssArray[nPos].trim());//13
//        nPos += 2;
//        rc.m_strSort = ssArray[nPos].trim();//8
//        nPos += 2;
//        rc.m_strEvent = ssArray[nPos].trim();//9
//        nPos += 2;
//        rc.m_nLayer =Integer.parseInt(ssArray[nPos].trim());//10
//        nPos += 2;
//        rc.m_nGrp = Integer.parseInt(ssArray[nPos].trim());//11
//        nPos += 2;
//        rc.m_nProperty = Integer.parseInt(ssArray[nPos].trim());//12
//        nPos += 2;
//        rc.m_strAllEventName = ssArray[nPos].trim();//13
//        nPos += 2;
//        rc.m_nAllProp = Integer.parseInt(ssArray[nPos].trim());//14
//        nPos += 2;
//        rc.m_nAllNum = Integer.parseInt(ssArray[nPos].trim());//15
//        nPos += 2;
//        rc.m_strWindSpeed = ssArray[nPos].trim();//16
//        nPos += 2;
//        rc.m_nField = Integer.parseInt(ssArray[nPos].trim());//17
//        nPos += 2;
//        rc.m_strBeginTime = ssArray[nPos].trim();//18
//        nPos += 2;
//        rc.m_nCheck = Integer.parseInt(ssArray[nPos].trim());//19
//        nPos += 2;
//        rc.m_nAffirmFlag = Integer.parseInt(ssArray[nPos].trim());//20
//        nPos += 2;
//        rc.m_nSporterNum = Integer.parseInt(ssArray[nPos].trim());//21
//        //////////////////////////////////////////////////////////////////////////
//        //曾辉添加
//        //////////////////////////////////////////////////////////////////////////
//        nPos += 2;
//        rc.m_strExamSitePlace = ssArray[nPos].trim();////考试场地//////////////
//        nPos += 2;
//        rc.m_nGroupType = ssArray[nPos].trim().length() > 0 ? Integer.parseInt(ssArray[nPos].trim()) : 0;////分组ID需要和服务器一致（考试中的分组类型 0-正常组，1-缓考组，2-补考组）//////////////
//        int i = 0;
//        for (i = 0; i < TCPConst.MAX_RECORD_COUNT; i++) {
//            nPos += 2;
//            rc.m_strRecordRank[i] = ssArray[nPos].trim();//21+i+1;
//        }
//        for (i = 0; i < TCPConst.MAX_RECORD_COUNT; i++) {
//            nPos += 2;
//            rc.m_strRecordResult[i] = ssArray[nPos].trim();//21+MAX_RECORD_COUNT+i+1;
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_nPlace[i] =  Integer.parseInt(ssArray[nPos].trim());//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*0
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_nTrackNO[i] = Integer.parseInt(ssArray[nPos].trim());//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*1
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strSporterID[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*2
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strSporterName[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*3
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strUnitName[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*4
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strResult[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*5
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strNote[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*6
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strSporterScoreCheck[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*7
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strJudgeTime[i] = ssArray[nPos].trim();//MAX_RECORD_COUNT*2+21+i+1+m_nSporterNum*8
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_nGender[i] =Integer.parseInt(ssArray[nPos].trim());//性别
//        }
//
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strUnitCode[i] = ssArray[nPos].trim();//单位代码//////////////
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strClassName[i] = ssArray[nPos].trim();//班级//////////////
//        }
//
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strStudentCategory[i] = ssArray[nPos].trim();//考生类别（正常，免考，择考）//////////////
//        }
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_strBestScore[i] = ssArray[nPos].trim();//最好成绩得分
//        }
//
//        for (i = 0; i < rc.m_nSporterNum; i++) {
//            nPos += 2;
//            rc.m_nExamStatus[i] = Integer.parseInt(ssArray[nPos].trim());//考试状态-正常，缓考，补考
//        }

        return 0;
    }


    public String EncodePackage(PackageHeadInfo headInfo,boolean bEnCrypt,int type)  {
        StringBuilder sb = new StringBuilder();
        String strBody = "";
        String target1 = null;
        String target2 = null;
        String target=null;
        try {
            target1 = new String(g_SepFlag1,"GB2312");
            target2 = new String(g_SepFlag,"GB2312");
            target=target1+target2;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        sb.append(m_strClientName);
        sb.append(target);
        sb.append(m_strServerName);
        sb.append(target);
        sb.append(m_lGrpID);
        sb.append(target);
        sb.append(m_nEventType);
        sb.append(target);
        sb.append(m_strPackType);
        sb.append(target);//22
        sb.append(m_strGameEventName);
        sb.append(target);
        sb.append(m_nSex);
        sb.append(target);
        sb.append(m_strSort);
        sb.append(target);
        sb.append(m_strEvent);
        sb.append(target);
        sb.append(m_nLayer);
        sb.append(target);
        sb.append(m_nGrp);
        sb.append(target);
        sb.append(m_nProperty);
        sb.append(target);
        sb.append(m_strAllEventName);
        sb.append(target);
        sb.append(m_nAllProp);
        sb.append(target);
        sb.append(m_nAllNum);
        sb.append(target);
        sb.append(m_strWindSpeed);
        sb.append(target);
        sb.append(m_nField);
        sb.append(target);
        sb.append(m_strBeginTime);
        sb.append(target);
        sb.append(m_nCheck);
        sb.append(target);
        sb.append(m_nAffirmFlag);
        sb.append(target);
        sb.append(m_nSporterNum);
        sb.append(target);
        sb.append(m_strExamSitePlace);
        sb.append(target);
        sb.append(m_nGroupType);
        sb.append(target);
        for (int i = 0; i < MAX_RECORD_COUNT ;i++) {
            sb.append(m_strRecordRank[i]);
            sb.append(target);
        }
        for (int i = 0; i < MAX_RECORD_COUNT; i++) {
            sb.append(m_strRecordResult[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_nPlace[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_nTrackNO[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strSporterID[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strSporterName[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strUnitName[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strResult[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strNote[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strSporterScoreCheck[i]);
            sb.append(target);
        }

        for (int i = 0; i < this.m_nSporterNum; i++) {
            sb.append(m_strJudgeTime[i]);
            sb.append(target);
        }


        //曾辉添加
        for (int i = 0; i < this.m_nSporterNum; i++) {
            //性别
            sb.append(m_nGender[i]);
            sb.append(target);
        }

        for (int i = 0; i < this.m_nSporterNum; i++) {
            //单位代码
            sb.append(m_strUnitCode[i]);
            sb.append(target);
        }

        for (int i = 0; i < this.m_nSporterNum; i++) {
            //班级//////////////
            sb.append(m_strClassName[i]);
            sb.append(target);
        }
        for (int i = 0; i < this.m_nSporterNum; i++) {
            //考生类别（正常，免考，择考）//////////////
            sb.append(m_strStudentCategory[i]);
            sb.append(target);
        }

        for (int i = 0; i < this.m_nSporterNum; i++) {
            //最好成绩得分
            sb.append(m_strBestScore[i]);
            sb.append(target);
        }

        for (int i = 0; i < this.m_nSporterNum; i++) {
            //考试状态-正常，缓考，补考
            sb.append(m_nExamStatus[i]);
            sb.append(target);
        }

        strBody = sb.toString();

        return  SetEncodePackageBuffer(headInfo,strBody,type,bEnCrypt, TCPConst.enmuCommand.CommandPTResult,false);

    }

    private String SetEncodePackageBuffer(PackageHeadInfo headInfo, String strBody, int type, boolean bEnCrypt, TCPConst.enmuCommand nCommandID, boolean bBasePack){

        String RS1 = "";
        String target = null;//特殊分割符的转换
        String target1 = null;
        try {
            target = new String(g_SepFlag,"GB2312");
            target1 = new String(g_SepFlag1,"GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int iTailLen = target1.getBytes().length+ headInfo.g_FPTail.getBytes().length + target.getBytes().length;
        //打包加上系统时间
        if (type > 0)
        {
            if (!bBasePack)
                headInfo.m_dwTickCount = (int) (System.currentTimeMillis()/1000);
            Log.i( "time----------------> ", headInfo.m_dwTickCount+"");
        }
        Log.i( "outBody-------------> ", strBody.getBytes().length+"" );


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
        Log.i( "nBodyLen---------> ", nBodyLen+"");
        if (bEnCrypt)
        {
            if (TCPConst.MAX_FPPACK_LEN < nBodyLen * 2)//只能使用明文
                headInfo.m_nEnCrypt = 0;
            else
                headInfo.m_nEnCrypt = 1;
        }
        else
            headInfo.m_nEnCrypt = 0;
        headInfo.m_nCommandID = nCommandID;

        try {
            headInfo.m_nCryptLength = strBody.getBytes("GB2312").length;//将包体内容转换成字节流后再计算包体的长度
            Log.i( "newBodyLen--------> ", strBody.trim().getBytes("GB2312").length+"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        headInfo.m_nTotalLength = TCPConst.HEAD_LEN + headInfo.m_nCryptLength + iTailLen;
        Log.i( "iTailLen--------->: ",iTailLen+"" );

        String strFPHead = target1+headInfo.g_FPHead +target ;//包头
     //   String strTotalLength = String.valueOf(headInfo.m_nTotalLength)+target2;//内容总长度
        String strTotalLength = String.format("%010d",headInfo.m_nTotalLength);
        String strCommandID = "";
        if (type == 0) {
            //     strCommandID = String.valueOf(TCPConst.enumEvent.EventData)+target2;
            strCommandID = String.format("%06d", TCPConst.enumEvent.EventScore.getIndex());
        }else {
            strCommandID = String.format("%06d",2);
        }

     //  String strCryptLength = String.valueOf(headInfo.m_nCryptLength)+target2;//内容包的长度
        String strCryptLength = String.format("%010d",headInfo.m_nCryptLength);

       String strCodeType = String.valueOf(TCPConst.enumCodeType.CodeGB2312.getIndex());//默认编码
       String strEnCrypt = String.valueOf(headInfo.m_nEnCrypt);//加密内容
       String strFPTail = target1+headInfo.g_FPTail + target;//包尾

        //回包组装
        if (type == 1 || type == 2)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strBody + strFPTail;
        else if (type == 3)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strBody + strFPTail;
        else if (type == 0)
            RS1 = strFPHead + strTotalLength + strCommandID + strCryptLength + strCodeType + strEnCrypt + strTickCount + strFPTail;
        int aa = strFPHead.getBytes().length;
        Log.i("headlength:-------> ", aa+"");

        return RS1;
    }

}
