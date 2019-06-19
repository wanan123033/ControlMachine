package com.feipulai.host.exl;

import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
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
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultExlWriter extends ExlWriter {
	
	int rowIndex;
	
	public ResultExlWriter(ExlListener listener){
		super(listener);
	}
	
	@Override
	protected void write(UsbFile file){
		String[] headers = {"学籍号","姓名","性别","项目","成绩","轮次","测试时间","成绩状态","备注"};
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("测试成绩");
		int width = 0;
		// 产生表格标题行,确定每个格子的长
		HSSFRow firstRow = sheet.createRow(0);
		for(short i = 0;i < headers.length;i++){
			HSSFCell cell = firstRow.createCell(i);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text);
			// 自动列宽
			width = headers[i].getBytes().length;
			sheet.setColumnWidth(i,width * 500);
		}
		
		// 所有该项目的报名信息
		// 这里以报名信息开始,因为报名信息是每个报名信息 学生 机器码 项目代码 的组合是唯一的
		List<StudentItem> studentItems = DBManager.getInstance()
				.querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(),TestConfigs.getCurrentItemCode());
		
		rowIndex = 1;
		
		generateRows(studentItems,sheet);
		
		// 身高体重,还需要生成体重成绩
		// TODO: 2019/3/12 身高体重导出成绩处理方式待定
		// if(TestConfigs.HEIGHT_ITEM_CODE.equals(TestConfigs.sCurrentItem.getItemCode())){
		// 	studentItems = DBManager.getInstance()
		// 			.querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(),TestConfigs.WEIGHT_ITEM_CODE);
		// 	generateRows(studentItems,sheet);
		// }
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
			Logger.i("exel文件导出成功,文件名:" + file.getName());
			listener.onExlResponse(ExlListener.EXEL_WRITE_SUCCESS, "Excel成绩导出成功");
			UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + file.getName() + "delete.exl");
			deleteFile.delete();
		} catch (IOException e) {
			Logger.i("Excel文件导出失败,文件写入失败");
			listener.onExlResponse(ExlListener.EXEL_WRITE_FAILED, "Excel文件导出失败,文件写入失败");
			e.printStackTrace();
		}
	}
	
	private void generateRows(List<StudentItem> studentItems,HSSFSheet sheet){
		List<RoundResult> roundResults;
		Student student;
		String itemName = TestConfigs.sCurrentItem.getItemName();
		
		for(StudentItem stuItem : studentItems){
			
			roundResults = DBManager.getInstance().queryResultsByStuItem(stuItem);
			student = DBManager.getInstance().queryStudentByStuCode(stuItem.getStudentCode());
			
			for(RoundResult result : roundResults){
				
				HSSFRow row = sheet.createRow(rowIndex ++ );
				HSSFCell cell = row.createCell(0);
				cell.setCellValue(student.getStudentCode());
				cell = row.createCell(1);
				cell.setCellValue(student.getStudentName());
				cell = row.createCell(2);
				cell.setCellValue(student.getSex() == Student.MALE ? "男" : "女");
				cell = row.createCell(3);
				cell.setCellValue(itemName);
				cell = row.createCell(4);
				cell.setCellValue(result.getResult());
				cell = row.createCell(5);
				cell.setCellValue(result.getRoundNo());
				cell = row.createCell(6);
				cell.setCellValue(result.getTestTime());
				cell = row.createCell(7);
				cell.setCellValue(result.getResultState() == RoundResult.RESULT_STATE_NORMAL ? "正常" : "犯规");
				
			}
		}
	}
	
}
