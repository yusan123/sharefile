package com.yu.controller;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Test {

    public static void main(String[] args) {

        File file = new File("E:/upload/zipkin-server-2.10.4-exec.jar");

        long l = file.lastModified();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //String format = dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault()));
        String format = dateTimeFormatter.format(Instant.ofEpochMilli(l));
        System.out.println(format);

    }

}
