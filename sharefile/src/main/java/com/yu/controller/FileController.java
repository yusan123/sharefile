package com.yu.controller;

import com.yu.entity.FileInfo;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TreeSet;
import java.util.UUID;

@Controller
public class FileController {

    @Value("${file.path}")
    private String filePath;

    //如果不配置默认10G
    @Value("${file.maxSpace:10240}")
    private long maxSpace;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void upload(MultipartFile file) {

        //判断剩余空间是否可以存储
        if (DataSize.ofBytes(file.getSize()).toMegabytes() > getRemainSpace(new File(filePath))) {
            throw new RuntimeException("剩余空间不足，请联系管理员处理！");
        }

        // 获取原始名字
        String oldFileName = file.getOriginalFilename();
        //处理文件名
        String newFileName = dealFileName(oldFileName);
        try {
            // 保存到服务器中
            file.transferTo(new File(filePath, newFileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String dealFileName(String fileName) {
        if (fileName.length() > 100) {
            fileName = fileName.substring(0, 100);
        }
        File file = new File(filePath, fileName);
        if (file.exists()) {
            // 获取后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            // 获取文件名
            String pureName = fileName.substring(0, fileName.lastIndexOf("."));
            fileName = pureName + "_"
                    + UUID.randomUUID().toString().replaceAll("-", "") + suffixName;
        }
        return fileName;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("files") MultipartFile[] files, Model model) {
        try {
            for (MultipartFile file : files) {
                upload(file);
            }
        } catch (Exception e) {
            model.addAttribute("errMsg", e.getMessage());
            //e.printStackTrace();
            return "err";
        }
        return "redirect:/";
    }


    @GetMapping("/download")
    public void download(@RequestParam String fileName, HttpServletResponse response) throws Exception {
        // 文件地址，真实环境是存放在数据库中的
        File file = new File(filePath, fileName);
        // 穿件输入对象
        FileInputStream fis = new FileInputStream(file);
        // 设置相关格式
        response.setContentType("application/force-download");
        // 设置下载后的文件名以及header
        response.addHeader("Content-disposition", "attachment;fileName=" + fileName);
        // 创建输出对象
        OutputStream os = response.getOutputStream();
        // 常规操作
        try {
            FileCopyUtils.copy(fis, os);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示文件列表及统计信息
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String listFiles(Model model) {
        File file = new File(filePath);
        File[] files = file.listFiles();

        TreeSet<FileInfo> data = new TreeSet<>();
        for (File f : files) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(f.getName());
            fileInfo.setSize(DataSize.ofBytes(f.length()).toMegabytes());
            fileInfo.setTimestamp(f.lastModified());
            fileInfo.setTime(formatTime(f.lastModified()));
            data.add(fileInfo);
        }

        model.addAttribute("fileNum", files.length);
        model.addAttribute("usedSpace", getUsedSpace(file));
        model.addAttribute("maxSpace", maxSpace);
        model.addAttribute("remainSpace", getRemainSpace(file));
        model.addAttribute("spaceUsageRate", getSpaceUsageRate(file));
        model.addAttribute("files", data);
        return "index";
    }

    /**
     * 时间戳转换为时间
     *
     * @param timeStamp
     * @return
     */
    private String formatTime(long timeStamp) {
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault()));
    }

    /**
     * 计算已使用的空间
     *
     * @return
     */
    private long getUsedSpace(File file) {
        DataSize dataSize = DataSize.ofBytes(FileUtils.sizeOfDirectory(file));
        return dataSize.toMegabytes();
    }

    /**
     * 计算空间使用率
     *
     * @return
     */
    private double getSpaceUsageRate(File file) {
        return Double.longBitsToDouble(getUsedSpace(file)) / Double.longBitsToDouble(maxSpace);
    }


    /**
     * 计算剩余空间
     *
     * @return
     */
    private long getRemainSpace(File file) {
        return Math.subtractExact(maxSpace, getUsedSpace(file));
    }


    /**
     * 删除文件
     *
     * @param fileName
     * @return
     */
    @GetMapping("/delete")
    public String delFile(@RequestParam String fileName) {
        File file = new File(filePath + fileName);
        file.delete();
        return "redirect:/";
    }
}
