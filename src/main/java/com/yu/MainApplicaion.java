package com.yu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplicaion {

    /**
     * 指定磁盘目录共享文件，可以上传下载删除，批量删除，批量下载，导出excel
     * 可以指定上传文件大小，总空间大小，支持对中文或特殊字符文件名文件上传
     *
     * 在linux上通过     * java -Dfile.encoding=utf-8 -jar sharefile-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(MainApplicaion.class, args);
    }
}
