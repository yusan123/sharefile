package com.yu.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo extends BaseRowModel implements Comparable, Serializable {
    @ExcelProperty
    private String fileName;
    private long timestamp;
    @ExcelProperty
    private String time;
    @ExcelProperty
    private long size;

    @Override
    public int compareTo(Object o) {
        FileInfo o1 = (FileInfo) o;
        return new Long(o1.getTimestamp() - timestamp).intValue();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
