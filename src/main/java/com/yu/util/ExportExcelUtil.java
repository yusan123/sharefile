package com.yu.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class ExportExcelUtil<T> {
    private final String sheetName;
    private final String[] rowName;
    private final List<T> dataList;
    private final OutputStream outputStream;

    public ExportExcelUtil(List<T> dataList, OutputStream outputStream) {
        this.dataList = dataList;
        this.rowName = getRowName();
        this.sheetName = getSheetName();
        this.outputStream = outputStream;
    }

    private String getSheetName() {
        if (dataList == null || dataList.isEmpty()) {
            return "default";
        }
        String modelName = dataList.get(0).getClass().getName();
        return (modelName != null && !modelName.isEmpty()) ? modelName : "default";
    }

    public void export() throws Exception {
        if (sheetName == null || sheetName.isEmpty() || rowName == null || rowName.length == 0) {
            throw new RuntimeException("传入对象集合为空不需要导出");
        }
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet(sheetName);
            HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);
            HSSFCellStyle style = getStyle(workbook);

            int columnNum = rowName.length;
            HSSFRow rowRowName = sheet.createRow(0);

            for (int n = 0; n < columnNum; n++) {
                HSSFCell cellRowName = rowRowName.createCell(n);
                cellRowName.setCellType(CellType.STRING);
                cellRowName.setCellValue(new HSSFRichTextString(rowName[n]));
                cellRowName.setCellStyle(columnTopStyle);
            }

            List<Map<String, Object>> objects = fromObjListToMapList();
            for (int i = 0; i < objects.size(); i++) {
                Map<String, Object> objectMap = objects.get(i);
                HSSFRow row = sheet.createRow(i + 1);
                for (int j = 0; j < rowName.length; j++) {
                    HSSFCell cell = row.createCell(j, CellType.STRING);
                    Object val = objectMap.get(rowName[j]);
                    if (val != null) {
                        cell.setCellValue(val.toString());
                    }
                    cell.setCellStyle(style);
                }
            }

            for (int colNum = 0; colNum < columnNum; colNum++) {
                int columnWidth = sheet.getColumnWidth(colNum) / 256;
                for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    HSSFRow currentRow = sheet.getRow(rowNum);
                    if (currentRow == null) {
                        currentRow = sheet.createRow(rowNum);
                    }
                    HSSFCell currentCell = currentRow.getCell(colNum);
                    if (currentCell != null && currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes("UTF-8").length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
                int width = colNum == 0 ? (columnWidth - 2) * 256 : (columnWidth + 4) * 256;
                sheet.setColumnWidth(colNum, Math.max(255, width));
            }

            workbook.write(outputStream);
            outputStream.flush();
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
            return Collections.emptyList();
        }
        Field[] fields = dataList.get(0).getClass().getDeclaredFields();
        List<Map<String, Object>> objects = new ArrayList<>();
        for (T obj : dataList) {
            Map<String, Object> objectMap = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                objectMap.put(field.getName(), field.get(obj));
            }
            objects.add(objectMap);
        }
        return objects;
    }

    private HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setFontName("Courier New");

        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setFontName("Courier New");

        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}