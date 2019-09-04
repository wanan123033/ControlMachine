package com.feipulai.common.exl;

import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by zzs on  2019/8/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ExlWriterUtil {

    private List<List<String>> writeData;

    private UsbFile file;
    private String sheetname = "sheet1";
    private ExlListener listener = new ExlListener() {
        @Override
        public void onExlResponse(int responseCode, String reason) {

        }
    };

    public ExlWriterUtil(UsbFile file) {
        this.file = file;
    }

    public void write() {
        new Thread() {
            @Override
            public void run() {
                // 声明一个工作薄
                HSSFWorkbook workbook = new HSSFWorkbook();
                // 生成一个表格
                HSSFSheet sheet = workbook.createSheet(sheetname);


                for (short i = 0; i < writeData.size(); i++) {
                    List<String> rowData = writeData.get(i);
                    // 产生表格标题行,确定每个格子的长
                    HSSFRow row = sheet.createRow(i);
                    for (int j = 0; j < rowData.size(); j++) {
                        HSSFCell cell = row.createCell(j);
                        if (i == 0) {
                            HSSFRichTextString text = new HSSFRichTextString(rowData.get(j));
                            cell.setCellValue(text);
                            // 自动列宽
                            int width = rowData.get(j).getBytes().length;
                            sheet.setColumnWidth(j, width * 500);
                        } else {
                            cell.setCellValue(rowData.get(j));
                        }
                    }
                }


                OutputStream fos;
                try {
                    if (file instanceof UsbFileAdapter) {
                        fos = new FileOutputStream(((UsbFileAdapter) file).getFile());
                    } else {
                        fos = new UsbFileOutputStream(file);
                    }
                    workbook.write(fos);
                    workbook.close();
                    fos.close();
                    if (listener != null) {
                        Logger.i("---> exel文件导出成功");
                        Logger.i("--->保存路径：" + file);
                        listener.onExlResponse(ExlListener.EXEL_WRITE_SUCCESS, "Excel成绩导出成功");
                    }
                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + file.getName() + "delete.exl");
                    deleteFile.delete();
                } catch (IOException e) {
                    if (listener != null) {
                        Logger.i("---> Excel文件导出失败,文件写入失败");
                        listener.onExlResponse(ExlListener.EXEL_WRITE_FAILED, "Excel文件导出失败,文件写入失败");
                    }
                    e.printStackTrace();
                }


            }
        }.start();
    }

    /**
     * 创建对话框
     *
     * @author wzl
     */
    public static class Builder {
        private ExlWriterUtil writerUtil;

        public Builder(UsbFile file) {
            writerUtil = new ExlWriterUtil(file);
        }


        public Builder setExlListener(ExlListener exlListener) {
            writerUtil.listener = exlListener;
            return this;
        }

        public Builder setWriteData(List<List<String>> writeData) {
            writerUtil.writeData = writeData;
            return this;
        }

        public Builder setSheetname(String sheetname) {
            writerUtil.sheetname = sheetname;
            return this;
        }

        public ExlWriterUtil build() {
            return writerUtil;
        }
    }
}
