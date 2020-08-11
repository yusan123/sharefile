package com.yu.controller;

import com.sun.jmx.snmp.SnmpUnknownModelLcdException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class FileController {

    @Value("${file.path}")
    private String filePath;

    //@Value("${spring.servlet.multipart.location}")
    private String filePath1;

    private String upload(MultipartFile file) {

        // 获取原始名字
        String fileName = file.getOriginalFilename();
        // 获取后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffixName);
        // 文件重命名，防止重复
        String pureName = fileName.substring(0, fileName.lastIndexOf("."));
        System.out.println(pureName);
        fileName = filePath + pureName + "_" + UUID.randomUUID().toString().replaceAll("-", "") + suffixName;
        // 文件对象
        File dest = new File(fileName);
        // 判断路径是否存在，如果不存在则创建
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            // 保存到服务器中
            file.transferTo(dest);
            return fileName + "上传成功" + System.lineSeparator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName + "上传失败" + System.lineSeparator();
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("files") MultipartFile[] files) {
        StringBuilder sb = new StringBuilder("上传结果：" + System.lineSeparator());

        for (MultipartFile file : files) {
            sb.append(upload(file));
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

    @GetMapping("/")
    public String listFiles(Model model) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        List<String> list = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        model.addAttribute("size", list.size());
        model.addAttribute("fileList", list);
        return "index";
    }

    @GetMapping("/delete")
    public String delFile(@RequestParam String fileName) {
        File file = new File(filePath + fileName);
        file.delete();
        return "redirect:/";
    }
}
