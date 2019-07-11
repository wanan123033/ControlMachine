package com.feipulai.exam.exl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by zzs on  2019/7/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class XlsxReaderUtil {
    private boolean isStop = false;
    private GetReaderXlsxDataListener dataListener;

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public GetReaderXlsxDataListener getDataListener() {
        return dataListener;
    }

    public void setDataListener(GetReaderXlsxDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public boolean read(InputStream inputStream) {
        try {

            OPCPackage pkg = OPCPackage.open(inputStream);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            int index = 0;
            while (iter.hasNext()) {
                InputStream stream = iter.next();
                String sheetName = iter.getSheetName();
                System.out.println(sheetName + " [index=" + index + "]:");
                DataFormatter formatter = new DataFormatter();
                InputSource sheetSource = new InputSource(stream);
                try {
                    XMLReader sheetParser = SAXHelper.newXMLReader();
                    ContentHandler handler = new XSSFSheetXMLHandler(
                            styles, null, strings, new SheetConvert(), formatter, false);
                    sheetParser.setContentHandler(handler);
                    sheetParser.parse(sheetSource);
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
                }
                stream.close();
                ++index;
            }
            return true;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OpenXML4JException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            return false;
        }
    }


    private class SheetConvert implements XSSFSheetXMLHandler.SheetContentsHandler {
        private boolean firstCellOfRow = false;
        private int currentRow = -1;
        private int currentCol = -1;
        private List<String> lineArray = new ArrayList<>();


        @Override
        public void startRow(int rowNum) {
            // Prepare for this row
            firstCellOfRow = true;
            currentRow = rowNum;
            currentCol = -1;
            lineArray.clear();


            if (isStop) {
                throw new RuntimeException("SAX startRow Stop");
            }
        }

        @Override
        public void endRow(int rowNum) {
            dataListener.readerLineData(rowNum, lineArray);
        }

        @Override
        public void cell(String cellReference, String formattedValue,
                         XSSFComment comment) {
            if (firstCellOfRow) {
                firstCellOfRow = false;
            }

            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if (cellReference == null) {
                cellReference = new CellRangeAddress(currentRow, currentRow, currentCol, currentCol).formatAsString();
            }

            // Did we miss any cells?
            int thisCol = (new CellReference(cellReference)).getCol();
            int missedCols = thisCol - currentCol - 1;
            for (int i = 0; i < missedCols; i++) {
                lineArray.add("");//如果读的单元格为空，则在行列表里添加空值
            }
            currentCol = thisCol;
            lineArray.add(formattedValue);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Skip, no headers or footers in CSV
        }
    }


    public interface GetReaderXlsxDataListener {
        void readerLineData(int rowNum, List<String> data);
    }
}
