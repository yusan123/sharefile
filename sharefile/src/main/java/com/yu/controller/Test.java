package com.yu.controller;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {

        File file = new File("E:/upload");

        File[] files = file.listFiles();

        System.out.println(Arrays.asList(files));

    }

}
