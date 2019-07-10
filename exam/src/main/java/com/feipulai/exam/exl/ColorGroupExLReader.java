package com.feipulai.exam.exl;

import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlReader;
import com.feipulai.common.utils.ExlPostfixUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.MiddleBean;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.db.MachineItemCodeUtil;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.ChipInfo;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.feipulai.exam.activity.MiddleDistanceRace.bean.MiddleBean.colorIds;

/**
 * @author ww 读取芯片分组Excel
 * @time 2019/7/9 10:14
 */
public class ColorGroupExLReader extends ExlReader {

    private List<String> mNecessaryCols;
    private Map<String, Integer> mColNums;
    private UsbFile file;
    private ChipInfo chipInfo;
    private ChipGroup chipGroup;

    public ColorGroupExLReader(ExlListener listener) {
        super(listener);
        String[] cols = {"颜", "芯片号", "芯片ID1", "芯片ID2"};
        mNecessaryCols = Arrays.asList(cols);
    }

    @Override
    protected void read(UsbFile file) {
        this.file = file;
        mColNums = new HashMap<>();
        List<ChipInfo> result = null;
        String postfix;
        if (file instanceof UsbFileAdapter) {
            Logger.i("文件路径：" + ((UsbFileAdapter) file).getFile());
            postfix = ExlPostfixUtil.getPostfix(((UsbFileAdapter) file).getFile().getName());
        } else {
            postfix = ExlPostfixUtil.getPostfix(file.getName());
        }
        if (!ExlPostfixUtil.EMPTY.equals(postfix)) {
            if (ExlPostfixUtil.OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
                result = readXls();
            } else if (ExlPostfixUtil.OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
                result = readXlsx();
            }
        } else {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "请选择正确的导入文件");
        }
        if (result == null || result.size() == 0) {
            return;
        }

        boolean success = insertIntoDB(result);
        if (success) {
            SettingHelper.getSystemSetting().setTestPattern(SystemSetting.PERSON_PATTERN);
            SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
            listener.onExlResponse(ExlListener.EXEL_READ_SUCCESS, "Excel导入成功!");
        }
    }

    private boolean insertIntoDB(List<ChipInfo> chipInfos) {
        List<Integer> chipNos = new ArrayList<>();
        List<ChipGroup> chipGroups = new ArrayList<>();
        ChipGroup chipGroup;
        //从芯片信息表中抽出颜色和人数来建立芯片颜色组表
        for (ChipInfo chipInfo : chipInfos
                ) {
            //把所有芯片号数字单独组成集合
            chipNos.add(chipInfo.getVestNo());
            //颜色组对象
            chipGroup = new ChipGroup();
            chipGroup.setColorGroupName(chipInfo.getColorGroupName());
            //颜色组的状态，暂时未用，待定
            if (chipInfo.getColorGroupName().contains("备用")) {
                chipGroup.setGroupType(1);
            } else {
                chipGroup.setGroupType(0);
            }
            chipGroups.add(chipGroup);
        }

        //取出最大的芯片号作为颜色组的人数
        int maxChipNo = Collections.max(chipNos);//芯片号最大值，来确定颜色组的人数

        //去除颜色组对象集合中的重复对象
        chipGroups = removeDuplicateChipGroup(chipGroups);

        Log.i("chipGroups", "---------" + chipGroups.toString());


        for (int i = 0; i < chipGroups.size(); i++) {
            //颜色组填充人数
            chipGroups.get(i).setStudentNo(maxChipNo);
            //颜色组填充颜色（已知固定颜色，在此随机分配，超出固定颜色数则不填充）
            if (colorIds.length > i) {
                chipGroups.get(i).setColor(colorIds[i]);
                //填充颜色到芯片信息组
                for (ChipInfo chip : chipInfos
                        ) {
                    if (chip.getColorGroupName().equals(chipGroups.get(i).getColorGroupName())) {
                        chip.setColor(colorIds[i]);
                    }
                }
            }
        }

        DBManager.getInstance().insertChipGroups(chipGroups);
        DBManager.getInstance().insertChipInfos(chipInfos);
        return true;
    }

    //根据指定属性去重 并按自然顺序排序
    public ArrayList<ChipGroup> removeDuplicateChipGroup(List<ChipGroup> chipGroups) {
        Set<ChipGroup> set = new TreeSet<>(new Comparator<ChipGroup>() {
            @Override
            public int compare(ChipGroup o1, ChipGroup o2) {
                return o1.getColorGroupName().compareTo(o2.getColorGroupName());
            }
        });
        set.addAll(chipGroups);
        return new ArrayList<>(set);
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
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,指定文件名" + file + "不存在");
            return null;
        } catch (Exception e) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
            return null;
        }
        return is;

    }

    private List<ChipInfo> readXlsx() {
        XSSFWorkbook xssfWorkbook;
        InputStream is = getInputStram();
        try {
            xssfWorkbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
            return null;
        }

        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);// HSSFSheet 标识某一页

        List<ChipInfo> result = readRow(xssfSheet);

        try {
            is.close();
            xssfWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "exel读取失败,文件读取异常");
            return null;
        }
        return result;
    }

    // 读取exel文档数据
    private List<ChipInfo> readXls() {
        InputStream is = getInputStram();
        HSSFWorkbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(getInputStram());
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
            return null;
        }
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);// HSSFSheet 标识某一页
        List<ChipInfo> result = readRow(hssfSheet);
        try {
            is.close();
            hssfWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "exel读取失败,文件读取异常");
            return null;
        }
        return result;
    }

    private List<ChipInfo> readRow(Sheet sheet) {
        if (sheet == null) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,请检查excel文件格式(Excel第一张表无内容)");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,请检查excel文件格式(Excel第一张表无内容)");
            return null;
        }
        // 处理第一行
        Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,请检查excel文件格式(Excel第一行无内容)");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,请检查excel文件格式(Excel第一行无内容)");
            return null;
        }

        // rowSum 总行数
        boolean isFirstRowRead = readFirstRow(firstRow);
        if (!isFirstRowRead) {
            return null;
        }

        // 到这里,文件格式就检查完成了,接下来解析真正的数据
        int rowSum = sheet.getPhysicalNumberOfRows();
        List<ChipInfo> result = new ArrayList<>();
        // 循环读取每一行,从第二行开始读
        for (int rowNum = 1; rowNum < rowSum; rowNum++) {

            Row row = sheet.getRow(rowNum);

            if (row == null) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取失败,第" + rowNum + "行读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取失败,第" + rowNum + "行读取失败");
                return null;
            }
            ChipInfo bean = generateBeanFromRow(row);
            if (bean == null) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取解析失败,第" + rowNum + "行读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取解析失败,第" + rowNum + "行读取失败");
                return null;
            }
            result.add(bean);
        }
        return result;
    }

    // 一行数据,生成一个对象,如果读取失败直接返回null
    private ChipInfo generateBeanFromRow(Row row) {
        try {
            Cell chipColorCell = row.getCell(mColNums.get("颜"));
            Cell chipNoCell = row.getCell(mColNums.get("芯片号"));
            Cell chipID1Cell = row.getCell(mColNums.get("芯片ID1"));
            Cell chipID2Cell = row.getCell(mColNums.get("芯片ID2"));
            if (chipColorCell == null || chipNoCell == null) {
                return null;
            }
            String chipColor = getStringVal(chipColorCell);
            String chipNo = getStringVal(chipNoCell);
            String chipID1 = getStringVal(chipID1Cell);
            String chipID2 = getStringVal(chipID2Cell);

            chipInfo = new ChipInfo();
            chipInfo.setChipID1(TextUtils.isEmpty(chipID1) ? "" : chipID1);
            chipInfo.setChipID2(TextUtils.isEmpty(chipID2) ? "" : chipID1);
            chipInfo.setColorGroupName(chipColor);
            chipInfo.setVestNo(Integer.parseInt(chipNo));

            return chipInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取第一行标题，检验格式是否正确，是否有缺失必填项
     *
     * @param firstRow 第一行
     * @return 第一行处理成功, 返回true;否则返回false
     */
    private boolean readFirstRow(Row firstRow) {
        int minColIx = firstRow.getFirstCellNum();
        int maxColIx = firstRow.getLastCellNum();
        for (int colIx = minColIx; colIx < maxColIx; colIx++) {
            // HSSFCell 表示单元格
            Cell cell = firstRow.getCell(colIx);
            if (cell == null) {
                continue;
            }
            //把需要的数据列的列名和索引记下来
            String cellValue = getStringVal(cell);
            // 必须有"(*)"号
            if (cellValue.contains("*")) {
                cellValue = cellValue.substring(0, cellValue.indexOf("*") - 1);
            }
            mColNums.put(cellValue, colIx);
        }
        //检查是否有所有需要的索引
        for (int i = 0; i < mNecessaryCols.size(); i++) {
            if (!mColNums.containsKey(mNecessaryCols.get(i))) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                return false;
            }
        }
        return true;
    }

    private String getStringVal(Cell cell) {
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

}
