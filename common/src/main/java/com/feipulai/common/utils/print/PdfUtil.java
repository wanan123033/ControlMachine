package com.feipulai.common.utils.print;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by zzs on  2020/8/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PdfUtil {


    /**
     * 根据图片生成PDF
     *
     * @param pdfPath       生成的PDF文件的路径
     * @param imagePathList 待生成PDF文件的图片集合
     * @throws IOException       可能出现的IO操作异常
     * @throws DocumentException PDF生成异常
     */
    public static void createPdf(String pdfPath, List<String> imagePathList) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

        //设置pdf背景
        PdfBackground event = new PdfBackground();
        writer.setPageEvent(event);

        document.open();
        for (int i = 0; i < imagePathList.size(); i++) {
            document.newPage();
            Image img = Image.getInstance(imagePathList.get(i));
            //设置图片缩放到A4纸的大小
            img.scaleToFit(PageSize.A4.getWidth() - 0 * 2, PageSize.A4.getHeight() - 0 * 2);
            //设置图片的显示位置（居中）
            img.setAbsolutePosition((PageSize.A4.getWidth() - img.getScaledWidth()) / 2, (PageSize.A4.getHeight() - img.getScaledHeight()) / 2);
            document.add(img);
        }
        document.close();
    }
}
