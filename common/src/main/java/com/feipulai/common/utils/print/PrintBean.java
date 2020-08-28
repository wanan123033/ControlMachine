package com.feipulai.common.utils.print;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zzs on  2020/8/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintBean implements Serializable {
    public static final String ENCRY_KEY = "fairplayA4Print1";
    private String title;//标题
    private String codeData;//条码生成内容
    private String printHand;
    private String printHandRight;
    private String[] printBottom; //底部长度固定为4
    private String[] printTableHand;//表格头长度固定为8
    private List<PrintDataBean> printDataBeans;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCodeData() {
        return codeData;
    }

    public void setCodeData(String codeData) {
        this.codeData = codeData;
    }

    public String getPrintHand() {
        return printHand;
    }

    public void setPrintHand(String printHand) {
        this.printHand = printHand;
    }

    public String getPrintHandRight() {
        return printHandRight;
    }

    public void setPrintHandRight(String printHandRight) {
        this.printHandRight = printHandRight;
    }

    public String[] getPrintBottom() {
        return printBottom;
    }

    public void setPrintBottom(String[] printBottom) {
        this.printBottom = printBottom;
    }

    public String[] getPrintTableHand() {
        return printTableHand;
    }

    public void setPrintTableHand(String[] printTableHand) {
        this.printTableHand = printTableHand;
    }

    public List<PrintDataBean> getPrintDataBeans() {
        return printDataBeans;
    }

    public void setPrintDataBeans(List<PrintDataBean> printDataBeans) {
        this.printDataBeans = printDataBeans;
    }

    public static class PrintDataBean implements Serializable {
        private String printString1;
        private String printString2;
        private String printString3;
        private String printString4;
        private String printString5;
        private String printString6;
        private String printString7;
        private String printString8;

        public PrintDataBean(String printString1, String printString2, String printString3, String printString4, String printString5) {
            this.printString1 = printString1;
            this.printString2 = printString2;
            this.printString3 = printString3;
            this.printString4 = printString4;
            this.printString5 = printString5;
        }

        public String getPrintString1() {
            return printString1;
        }

        public void setPrintString1(String printString1) {
            this.printString1 = printString1;
        }

        public String getPrintString2() {
            return printString2;
        }

        public void setPrintString2(String printString2) {
            this.printString2 = printString2;
        }

        public String getPrintString3() {
            return printString3;
        }

        public void setPrintString3(String printString3) {
            this.printString3 = printString3;
        }

        public String getPrintString4() {
            return printString4;
        }

        public void setPrintString4(String printString4) {
            this.printString4 = printString4;
        }

        public String getPrintString5() {
            return printString5;
        }

        public void setPrintString5(String printString5) {
            this.printString5 = printString5;
        }

        public String getPrintString6() {
            return printString6;
        }

        public void setPrintString6(String printString6) {
            this.printString6 = printString6;
        }

        public String getPrintString7() {
            return printString7;
        }

        public void setPrintString7(String printString7) {
            this.printString7 = printString7;
        }

        public String getPrintString8() {
            return printString8;
        }

        public void setPrintString8(String printString8) {
            this.printString8 = printString8;
        }

        @Override
        public String toString() {
            return "PrintDataBean{" +
                    "printString1='" + printString1 + '\'' +
                    ", printString2='" + printString2 + '\'' +
                    ", printString3='" + printString3 + '\'' +
                    ", printString4='" + printString4 + '\'' +
                    ", printString5='" + printString5 + '\'' +
                    ", printString6='" + printString6 + '\'' +
                    ", printString7='" + printString7 + '\'' +
                    ", printString8='" + printString8 + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PrintBean{" +
                "title='" + title + '\'' +
                ", codeData='" + codeData + '\'' +
                ", printHand='" + printHand + '\'' +
                ", printHandRight='" + printHandRight + '\'' +
                ", printBottom=" + Arrays.toString(printBottom) +
                ", printTableHand=" + Arrays.toString(printTableHand) +
                ", printDataBeans=" + printDataBeans +
                '}';
    }
}
