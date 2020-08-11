package com.yu.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2019/8/2.
 */
public class ExportExcelUtil<T> {

    //显示的导出表的标题
    private String sheetName;
    //导出表的列名
    private String[] rowName;
    private List<T> dataList;

    private OutputStream outputStream;

    /**
     * 创建导出工具类时只需要传入数据集合和输出流即可
     * @param dataList 数据集合
     * @param outputStream 输出流
     */
    public ExportExcelUtil(List<T> dataList, OutputStream outputStream) {
        this.dataList = dataList;
        this.rowName = getRowName();
        this.sheetName = getSheetName();
        this.outputStream = outputStream;
    }

    private String getSheetName() {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        String modelName = dataList.get(0).getClass().getName();
        if (modelName == null || modelName == "") {
            return "default";
        }
        return modelName;
    }

    /**
     *
     * @throws Exception
     */
    public void export() throws Exception {
        if (sheetName == null || sheetName == "" || rowName == null || rowName.length == 0) {
            throw new RuntimeException("传入对象集合为空不需要导出");
        }
        HSSFWorkbook workbook = new HSSFWorkbook();                        // 创建工作簿对象
        HSSFSheet sheet = workbook.createSheet(sheetName);                    // 创建工作表
        //sheet样式定义【getColumnTopStyle()/getStyle()均为自定义方法 - 在下面  - 可扩展】
        HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);//获取列头样式对象
        HSSFCellStyle style = getStyle(workbook);                    //单元格样式对象
        // 定义所需列数
        int columnNum = rowName.length;
        HSSFRow rowRowName = sheet.createRow(0);                // 在索引2的位置创建行(最顶端的行开始的第二行)

        // 将列头设置到sheet的单元格中
        for (int n = 0; n < columnNum; n++) {
            HSSFCell cellRowName = rowRowName.createCell(n);                //创建列头对应个数的单元格
            cellRowName.setCellType(CellType.STRING);                //设置列头单元格的数据类型
            HSSFRichTextString text = new HSSFRichTextString(rowName[n]);
            cellRowName.setCellValue(text);                                    //设置列头单元格的值
            cellRowName.setCellStyle(columnTopStyle);                        //设置列头单元格样式
        }

        //将查询出的数据设置到sheet对应的单元格中
        List<Map<String, Object>> objects = fromObjListToMapList();
        for (int i = 0; i < objects.size(); i++) {
            Map<String, Object> objectMap = objects.get(i);//遍历每个对象
            HSSFRow row = sheet.createRow(i + 1); //创建所需的行数
            for (int j = 0; j < rowName.length; j++) {
                HSSFCell cell = row.createCell(j, CellType.STRING);
                Object val = objectMap.get(rowName[j]);
                if (val != null) {
                    cell.setCellValue(val.toString());                        //设置单元格的值
                }
                cell.setCellStyle(style);                                    //设置单元格样式
            }
        }
        //让列宽随着导出的列长自动适应
        for (int colNum = 0; colNum < columnNum; colNum++) {
            int columnWidth = sheet.getColumnWidth(colNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                HSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }
                if (currentRow.getCell(colNum) != null) {
                    HSSFCell currentCell = currentRow.getCell(colNum);
                    if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes("UTF-8").length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            if (colNum == 0) {
                sheet.setColumnWidth(colNum, (columnWidth - 2) * 256);
            } else {
                sheet.setColumnWidth(colNum, (columnWidth + 4) * 256);
            }
        }

        if (workbook != null) {
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }

    }

    private String[] getRowName() {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        Field[] fields = dataList.get(0).getClass().getDeclaredFields();
        String[] rowName = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            rowName[i] = fields[i].getName();
        }
        return rowName;
    }

    private List<Map<String, Object>> fromObjListToMapList() throws IllegalAccessException {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        Field[] fields = dataList.get(0).getClass().getDeclaredFields();
        List<Map<String, Object>> objects = new ArrayList<>();
        for (T obj : dataList) {
            Map<String, Object> objectMap = new HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                objectMap.put(fields[i].getName(), fields[i].get(obj));
            }
            objects.add(objectMap);
        }
        return objects;
    }

    /*
     * 列头单元格样式
     */
    public HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {

        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBold(true);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;

    }

    /*
     * 列数据信息单元格样式
     */
    public HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        //font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;

    }
}