package com.yu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class FileToZip {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileToZip.class);
    private static final int BUFFER_SIZE = 8192;

    private FileToZip() {}

    public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            LOGGER.warn("待压缩的文件目录：{} 不存在", sourceFilePath);
            return false;
        }

        File zipFile = new File(zipFilePath, fileName + ".zip");
        if (zipFile.exists()) {
            LOGGER.warn("{} 目录下存在名字为：{} 打包文件", zipFilePath, fileName);
            return false;
        }

        File[] sourceFiles = sourceFile.listFiles();
        if (sourceFiles == null || sourceFiles.length < 1) {
            LOGGER.warn("待压缩的文件目录：{} 里面不存在文件,无需压缩", sourceFilePath);
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            for (File file : sourceFiles) {
                if (file.isDirectory() || file.getName().equals("tmp")) {
                    continue;
                }
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE)) {
                    int read;
                    while ((read = bis.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                }
                zos.closeEntry();
            }
            LOGGER.info("文件打包成功: {}", zipFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.error("文件打包失败", e);
            return false;
        }
    }
}
