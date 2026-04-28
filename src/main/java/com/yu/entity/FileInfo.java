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
public class FileInfo extends BaseRowModel implements Comparable<FileInfo>, Serializable {
    @ExcelProperty
    private String fileName;
    private long timestamp;
    @ExcelProperty
    private String time;
    @ExcelProperty
    private long size;

    @Override
    public int compareTo(FileInfo o) {
        return Long.compare(o.getTimestamp(), timestamp);
    }
}
