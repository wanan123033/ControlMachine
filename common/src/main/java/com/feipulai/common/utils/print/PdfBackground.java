package com.feipulai.common.utils.print;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Created by zzs on  2020/8/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PdfBackground extends PdfPageEventHelper {

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        //设置pdf背景色为白色
        PdfContentByte canvas = writer.getDirectContentUnder();
        Rectangle rect = document.getPageSize();
        canvas.setColorFill(BaseColor.WHITE);
        canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        canvas.fill();

        //设置pdf页面内间距
        PdfContentByte canvasBorder = writer.getDirectContent();
        Rectangle rectBorder = document.getPageSize();
        rectBorder.setBorder(Rectangle.BOX);
        rectBorder.setBorderColor(BaseColor.WHITE);
        rectBorder.setUseVariableBorders(true);
        canvasBorder.rectangle(rectBorder);
    }

}
