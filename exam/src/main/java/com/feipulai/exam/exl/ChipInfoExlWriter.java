package com.feipulai.exam.exl;

import android.text.TextUtils;

import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipInfo;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author ww
 * 导出芯片表
 * @time 2019/7/10 9:16
 */
public class ChipInfoExlWriter extends ExlWriter {

    int rowIndex;

    public ChipInfoExlWriter(ExlListener listener) {
        super(listener);
    }

    @Override
    protected void write(UsbFile file) {
        String[] headers = new String[5];
        String[] first = new String[]{"序号", "颜", "芯片号", "芯片ID1", "芯片ID2"};
        System.arraycopy(first, 0, headers, 0, first.length);

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet("芯片信息");
        int width = 0;
        // 产生表格标题行,确定每个格子的长
        HSSFRow firstRow = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = firstRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
            // 自动列宽
            width = headers[i].getBytes().length;
            sheet.setColumnWidth(i, width * 500);
        }

        List<ChipInfo> chipInfos = DBManager.getInstance().queryAllChipInfo();
        rowIndex = 1;

        generateRows(chipInfos, sheet);

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
                Logger.i(TestConfigs.df.format(new Date()) + "---> exel文件导出成功");
                Logger.i(TestConfigs.df.format(new Date()) + "--->保存路径：" + file);
                listener.onExlResponse(ExlListener.EXEL_WRITE_SUCCESS, "Excel导出成功");
            }
            UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + file.getName() + "delete.exl");
            deleteFile.delete();
        } catch (IOException e) {
            if (listener != null) {
                Logger.i(TestConfigs.df.format(new Date()) + "---> Excel文件导出失败,文件写入失败");
                listener.onExlResponse(ExlListener.EXEL_WRITE_FAILED, "Excel文件导出失败,文件写入失败");
            }
            e.printStackTrace();
        }

    }

    private void generateRows(List<ChipInfo> chipInfos, HSSFSheet sheet) {
        for (int j = 0; j < chipInfos.size(); j++) {
            HSSFRow row = sheet.createRow(rowIndex++);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(j+1);
            cell = row.createCell(1);
            cell.setCellValue(chipInfos.get(j).getColorGroupName());
            cell = row.createCell(2);
            cell.setCellValue(chipInfos.get(j).getVestNo());
            cell = row.createCell(3);
            cell.setCellValue(TextUtils.isEmpty(chipInfos.get(j).getChipID1())?"":chipInfos.get(j).getChipID1());
            cell = row.createCell(4);
            cell.setCellValue(TextUtils.isEmpty(chipInfos.get(j).getChipID2())?"":chipInfos.get(j).getChipID1());
        }
    }

}
