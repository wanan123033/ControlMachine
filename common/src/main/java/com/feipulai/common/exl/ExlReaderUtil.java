package com.feipulai.common.exl;

import android.text.TextUtils;

import com.feipulai.common.dbutils.UsbFileAdapter;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzs on  2019/8/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ExlReaderUtil {
    private boolean isStop = false;
    private int cellLength = 0;


    private ExlListener listener=new ExlListener() {
        @Override
        public void onExlResponse(int responseCode, String reason) {

        }
    };
    private GetReaderDataListener readerDataListener=new GetReaderDataListener() {
        @Override
        public void readerLineData(int rowNum, List<String> data) {

        }
    };
    private UsbFile file;
    private XlsxReaderUtil xlsxReader;

    public ExlReaderUtil(UsbFile file) {
        this.file = file;
    }

    public void read() {
        String postfix;
        if (file instanceof UsbFileAdapter) {
            Logger.i("文件路径：" + ((UsbFileAdapter) file).getFile());
            postfix = ExlPostfixUtil.getPostfix(((UsbFileAdapter) file).getFile().getName());
        } else {
            postfix = ExlPostfixUtil.getPostfix(file.getName());
        }
        if (!ExlPostfixUtil.EMPTY.equals(postfix)) {
            if (ExlPostfixUtil.OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
                readXls();
            } else if (ExlPostfixUtil.OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
                readXlsx();
            }
        } else {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "请选择正确的导入文件");
        }
    }


    // 读取exel文档数据
    private void readXls() {
        InputStream is = getInputStram();
        HSSFWorkbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(getInputStram());
            Logger.i("HSSFWorkbook");
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);// HSSFSheet 标识某一页
            int rowSum = hssfSheet.getPhysicalNumberOfRows();
            // 循环读取每一行,从第二行开始读
            for (int rowNum = 0; rowNum < rowSum; rowNum++) {
                if (!isStop) {
                    Row row = hssfSheet.getRow(rowNum);
                    if (row == null) {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取失败,第" + rowNum + "行读取失败");
                    }
                    List<String> rowData = new ArrayList<>();
                    for (int i = 0; i < cellLength; i++) {
                        String cellString = getStringVal(row.getCell(i));
                        rowData.add(TextUtils.isEmpty(cellString) ? "" : cellString);
                    }
                    readerDataListener.readerLineData(rowNum, rowData);
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
        }
    }

    private void readXlsx() {
        xlsxReader = new XlsxReaderUtil();
        xlsxReader.setDataListener(readerDataListener);
        try {
            xlsxReader.read(getInputStram());
            xlsxReader.setStop(isStop);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
        }
    }

    public void setStop(boolean stop) {
        isStop = stop;
        if (xlsxReader != null) {
            xlsxReader.setStop(stop);
        }
    }

    private InputStream getInputStram() {
        InputStream is = null;
        try {
            if (file instanceof UsbFileAdapter) {
                Logger.i("文件路径：" + ((UsbFileAdapter) file).getFile());
                is = new FileInputStream(((UsbFileAdapter) file).getFile());
            } else {
                is = new UsbFileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,指定文件名" + file + "不存在");
            return null;
        } catch (Exception e) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            return null;
        }
        return is;

    }

    private String getStringVal(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 创建对话框
     *
     * @author wzl
     */
    public static class Builder {
        private ExlReaderUtil readerUtil;

        public Builder(UsbFile file) {
            readerUtil = new ExlReaderUtil(file);
        }

        public Builder setReaderDataListener(GetReaderDataListener readerDataListener) {
            readerUtil.readerDataListener = readerDataListener;
            return this;
        }

        public Builder setExlListener(ExlListener exlListener) {
            readerUtil.listener = exlListener;
            return this;
        }

        public Builder setCellLength(int cellLength) {
            readerUtil.cellLength = cellLength;
            return this;
        }

        /**
         * 创建对话框
         *
         * @return
         */
        public ExlReaderUtil build() {
            return readerUtil;
        }
    }
}
