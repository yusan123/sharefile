package com.yu.controller;

import com.yu.entity.FileInfo;
import com.yu.exception.ShareFileException;
import com.yu.util.ExportExcelUtil;
import com.yu.util.FileToZip;
import com.yu.util.ThreadPoolUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Controller
public class FileController {

    @Value("${file.path}")
    private String filePath;

    @Value("${file.isOpenComplexChar:true}")
    private boolean isOpenComplexChar;

    @Value("${file.isOpenDeleteAll:false}")
    private boolean isOpenDeleteAll;

    @Value("${file.isOpenDelete:true}")
    private boolean isOpenDelete;

    @Value("${file.isOpenUpload:true}")
    private boolean isOpenUpload;

    @Value("${file.isOpenDownload:true}")
    private boolean isOpenDownload;

    @Value("${file.isOpenDownloadAll:true}")
    private boolean isOpenDownloadAll;

    //如果不配置默认1G
    @Value("${file.maxSpace:1024}")
    private long maxSpace;

    @Value("#{'${spring.servlet.multipart.max-file-size}'.replaceAll('MB','')}")
    private int maxFileSize;

    @Value("#{'${spring.servlet.multipart.max-request-size}'.replaceAll('MB','')}")
    private int maxRequestSize;

    private static final String SHAREFILE = "sharefile";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    private StopWatch stopWatch = new StopWatch();

    @Autowired
    private HttpServletRequest request;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 验证文件路径安全性，防止路径遍历攻击
     */
    private void validateFilePath(String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ShareFileException("非法文件名");
        }
    }

    private void upload(MultipartFile file) {

        //判断剩余空间是否可以存储
        long needSpace = DataSize.ofBytes(file.getSize()).toMegabytes();
        long remainSpace = getRemainSpace(new File(filePath));
        if (needSpace > remainSpace) {
            throw new ShareFileException("剩余空间不足，请联系管理员处理！");
        }
        // 获取原始名字
        String oldFileName = file.getOriginalFilename();
        //处理文件名
        String newFileName = dealFileName(oldFileName);
        LOGGER.info(String.format("开始上传文件原名为%s,新名为%s,需要空间%sMB,剩余空间为%sMB",
                oldFileName, newFileName, needSpace, remainSpace));
        try {
            // 保存到服务器中
            file.transferTo(new File(filePath, newFileName));
        } catch (IOException e) {
            LOGGER.error(String.format("上传文件失败，原名为%s,新名为%s", oldFileName, newFileName), e);
            throw new ShareFileException("文件上传失败：" + e.getMessage());
        }
        LOGGER.info(String.format("开始上传文件原名为%s,新名为%s,上传成功！", oldFileName, newFileName));
    }

    /**
     * 多文件上传前检查总空间（避免并发竞态）
     */
    private void validateTotalSpace(MultipartFile[] files) {
        long totalNeed = 0;
        for (MultipartFile file : files) {
            totalNeed += DataSize.ofBytes(file.getSize()).toMegabytes();
        }
        long remainSpace = getRemainSpace(new File(filePath));
        if (totalNeed > remainSpace) {
            throw new ShareFileException("总上传空间不足，请联系管理员处理！");
        }
    }

    /**
     * 删除所有文件，重要！！需要管理员权限
     *
     * @param model
     * @return
     * @throws IOException
     */
    @GetMapping("/delAll")
    public String delAll(Model model) throws IOException {
        if (!isOpenDeleteAll) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        File file = new File(filePath);
        FileUtils.cleanDirectory(file);
        // 根据请求来源决定重定向目标
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/new")) {
            return "redirect:/new";
        }
        return "redirect:/";
    }

    @GetMapping("/downloadAll")
    public void downloadAllZip(HttpServletResponse response) throws IOException {
        if (!isOpenDownloadAll) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        String tmpZipPath = filePath + "/tmp";
        File tempPath = new File(tmpZipPath);
        try {
            if (!tempPath.exists()) {
                tempPath.mkdirs();
            }
            //压缩的过程
            boolean zipRes = FileToZip.fileToZip(filePath, tmpZipPath, SHAREFILE);
            LOGGER.info(String.format("压缩文件%s！即将开始下载！", zipRes ? "成功" : "失败"));
            if (zipRes) {
                String zipFileName = SHAREFILE + ".zip";
                File file = new File(tmpZipPath, zipFileName);
                downloadFile(zipFileName, response, file);
                LOGGER.info("打包批量下载成功！");
            }
        } finally {
            //下载完成后清理临时数据
            FileUtils.deleteDirectory(tempPath);
        }
    }

    private String dealFileName(String fileName) {
        // 获取后缀名（截断前先保存，避免截断丢失扩展名）
        int lastDot = fileName.lastIndexOf(".");
        String suffixName = lastDot > 0 ? fileName.substring(lastDot) : "";
        String pureName = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;

        //超长处理（保留扩展名）
        if (pureName.length() > 58) {
            pureName = pureName.substring(0, 58);
        }
        fileName = pureName + suffixName;

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        if (!isOpenComplexChar) {
            //如果关闭支持特殊字符后，文件包含特殊字符，则将文件名改为uuid
            if (fileName.length() != fileName.replaceAll("[^\\w-_.]", "").length()) {
                //认为包含特殊字符
                return uuid + suffixName;
            }
        }
        //重名处理
        File file = new File(filePath, fileName);
        if (file.exists()) {
            fileName = pureName + "_" + uuid + suffixName;
        }
        return fileName;
    }

    /**
     * 文件上传单线程
     *
     * @param files
     * @param model
     * @return
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("files") MultipartFile[] files, Model model) {
        if (!isOpenUpload) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        if (files[0] == null || StringUtils.isEmpty(files[0].getOriginalFilename())) {
            throw new ShareFileException("你没有选择任何文件，请选择文件后再上传！");
        }
        // 上传前预先检查总空间（避免并发竞态）
        validateTotalSpace(files);
        stopWatch.start();
        try {
            for (MultipartFile file : files) {
                upload(file);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            stopWatch.stop();
        }
        double totalTimeSeconds = stopWatch.getTotalTimeSeconds();
        LOGGER.info("本次上传共耗时:" + totalTimeSeconds + "秒！");
        model.addAttribute("time", totalTimeSeconds);
        // 根据请求来源决定重定向目标
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/new")) {
            return "redirect:/new?time=" + totalTimeSeconds;
        }
        return "redirect:/?time=" + totalTimeSeconds;
    }

    /**
     * 文件上传多线程版本
     * 多线程上传目前有问题，因为多个文件同时上传，去判断磁盘剩余空间时就会有问题，导致上传的比限制的多
     * 而且一旦剩余空间不够，要抛异常，在thread里异常抛不出来
     *
     * @param files
     * @param model
     * @return
     */
    @PostMapping("/upload1")
    public String uploadUseThread(@RequestParam("files") MultipartFile[] files, Model model) throws InterruptedException {
        if (!isOpenUpload) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        if (files[0] == null || StringUtils.isEmpty(files[0].getOriginalFilename())) {
            throw new ShareFileException("你没有选择任何文件，请选择文件后再上传！");
        }
        validateTotalSpace(files);
        stopWatch.start();
        CountDownLatch countDownLatch = new CountDownLatch(files.length);
        for (MultipartFile file : files) {
            ThreadPoolUtil.submit(() -> {
                upload(file);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        stopWatch.stop();
        double totalTimeSeconds = stopWatch.getTotalTimeSeconds();
        LOGGER.info("本次上传共耗时:" + totalTimeSeconds + "秒！");
        model.addAttribute("time", totalTimeSeconds);
        return "redirect:/";
    }

    /**
     * 使用easyExcel导出
     * @param response
     * @throws Exception
     */
    /*@GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        ExcelWriter writer = null;
        OutputStream outputStream = response.getOutputStream();
        try {
            //添加响应头信息
            response.setHeader("Content-disposition", "attachment; filename=" + "files.xls");
            response.setContentType("application/msexcel;charset=UTF-8");//设置类型
            response.setHeader("Pragma", "No-cache");//设置头
            response.setHeader("Cache-Control", "no-cache");//设置头
            response.setDateHeader("Expires", 0);//设置日期头

            //实例化 ExcelWriter
            writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLS, true);

            //实例化表单
            Sheet sheet = new Sheet(1, 0, FileInfo.class);
            sheet.setSheetName("目录");

            //获取数据
            File[] files = new File(filePath).listFiles();
            TreeSet<FileInfo> fileInfos = getFileInfos(files);

            //输出
            writer.write(new ArrayList<>(fileInfos), sheet);
            writer.finish();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }*/

    /**
     * 使用poi工具类导出
     *
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        //添加响应头信息
        response.setHeader("Content-disposition", "attachment; filename=" + "files.xls");
        response.setContentType("application/msexcel;charset=UTF-8");//设置类型
        TreeSet<FileInfo> fileInfos = getFileInfos(new File(filePath).listFiles());
        ExportExcelUtil<FileInfo> exportExcelUtil = new ExportExcelUtil<>(new ArrayList<>(fileInfos), outputStream);
        exportExcelUtil.export();
    }

    @GetMapping("/download")
    public void download(@RequestParam String fileName, HttpServletResponse response) throws Exception {
        if (!isOpenDownload) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        validateFilePath(fileName);
        LOGGER.info("开始下载文件: " + fileName);
        // 文件地址，真实环境是存放在数据库中的
        File file = new File(filePath, fileName);
        downloadFile(fileName, response, file);
        LOGGER.info("成功下载文件: " + fileName);
    }

    /**
     * 在线预览文件
     */
    @GetMapping("/preview")
    public void preview(@RequestParam String fileName, HttpServletResponse response) throws Exception {
        validateFilePath(fileName);
        LOGGER.info("开始预览文件: " + fileName);
        File file = new File(filePath, fileName);
        if (!file.exists()) {
            throw new ShareFileException("文件不存在");
        }

        String contentType = getContentType(fileName);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline;fileName=" + new String(fileName.getBytes("UTF-8"), "iso-8859-1"));

        FileInputStream fis = null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(file);
            os = response.getOutputStream();
            FileCopyUtils.copy(fis, os);
        } finally {
            try {
                if (fis != null) fis.close();
                if (os != null) os.close();
            } catch (IOException e) {
                LOGGER.warn("关闭流失败", e);
            }
        }
    }

    /**
     * 根据文件名获取 Content-Type
     */
    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                   lowerName.endsWith(".gif") || lowerName.endsWith(".bmp") || lowerName.endsWith(".webp")) {
            return "image/" + (lowerName.endsWith(".jpg") ? "jpeg" : lowerName.substring(lowerName.lastIndexOf(".") + 1));
        } else if (lowerName.endsWith(".txt") || lowerName.endsWith(".log") || lowerName.endsWith(".md") ||
                   lowerName.endsWith(".json") || lowerName.endsWith(".xml") || lowerName.endsWith(".html") ||
                   lowerName.endsWith(".css") || lowerName.endsWith(".js")) {
            return "text/plain;charset=UTF-8";
        } else if (lowerName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".ogg")) {
            return "audio/" + lowerName.substring(lowerName.lastIndexOf(".") + 1);
        }
        return "application/octet-stream";
    }

    private void downloadFile(String fileName, HttpServletResponse response, File file) {
        FileInputStream fis = null;
        OutputStream os = null;
        try {
            // 创建输入对象
            fis = new FileInputStream(file);
            // 设置相关格式
            response.setContentType("application/octet-stream");
            // 设置下载后的文件名以及header
            response.addHeader("Content-Disposition",
                    "attachment;fileName=" + new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
            // 创建输出对象
            os = response.getOutputStream();
            // 常规操作
            FileCopyUtils.copy(fis, os);
        } catch (IOException e) {
            throw new ShareFileException(e.getMessage());
        } finally {
            try {
                if (fis != null) fis.close();
                if (os != null) os.close();
            } catch (IOException e) {
                LOGGER.warn("关闭流失败", e);
            }
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
        return listFilesCommon(model, "new_index");
    }
    
    @GetMapping("/old")
    public String listFilesOld(Model model) {
        return listFilesCommon(model, "index");
    }
    
    private String listFilesCommon(Model model, String viewName) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] files = file.listFiles();

        TreeSet<FileInfo> data = getFileInfos(files);

        String time = request.getParameter("time");
        model.addAttribute("time", time);
        model.addAttribute("fileNum", files.length);
        model.addAttribute("filePath", filePath);
        model.addAttribute("usedSpace", getUsedSpace(file));
        model.addAttribute("maxSpace", maxSpace);
        model.addAttribute("maxFileSize", maxFileSize);
        model.addAttribute("maxRequestSize", maxRequestSize);
        model.addAttribute("remainSpace", getRemainSpace(file));
        model.addAttribute("spaceUsageRate", getSpaceUsageRate(file));
        model.addAttribute("files", data);

        //设置开关选项
        model.addAttribute("isOpenDelete", isOpenDelete);
        model.addAttribute("isOpenDeleteAll", isOpenDeleteAll);
        model.addAttribute("isOpenDownload", isOpenDownload);
        model.addAttribute("isOpenDownloadAll", isOpenDownloadAll);
        model.addAttribute("isOpenUpload", isOpenUpload);
        return viewName;
    }

    private TreeSet<FileInfo> getFileInfos(File[] files) {
        TreeSet<FileInfo> data = new TreeSet<>();
        for (File f : files) {
            //不展示文件夹
            if (f.isDirectory()) {
                continue;
            }
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(f.getName());
            fileInfo.setSize(DataSize.ofBytes(f.length()).toMegabytes());
            fileInfo.setTimestamp(f.lastModified());
            fileInfo.setTime(formatTime(f.lastModified()));
            data.add(fileInfo);
        }
        return data;
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
        double used = getUsedSpace(file) * 100.0;
        double max = maxSpace;
        return Double.parseDouble(String.format("%.2f", used / max));
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
    public String delFile(@RequestParam String fileName) throws IOException {
        if (!isOpenDelete) {
            throw new ShareFileException("功能未开启，联系管理员开启此功能！");
        }
        validateFilePath(fileName);
        /**
         * java 流的close方法同system.gc方法只是告诉jvm，这里需要清理，但不一定立刻被清理，
         * 所以在上传完文件后，立刻删除文件会提示文件被占用
         * 解决办法：在调用file.delete()之前，调用System.gc(),就不会出现文件被占用的情况了
         */
        System.gc();
        File file = new File(filePath, fileName);
        FileUtils.forceDelete(file);
        // 根据请求来源决定重定向目标
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/new")) {
            return "redirect:/new";
        }
        return "redirect:/";
    }
}
