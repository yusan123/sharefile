## 简介
### 可以实现指定大小的磁盘空间作为共享网盘，可以单独设置开启上传下载，删除，打包下载，批量删除等功能.
### 主要技术使用```springboot```和```thymeleaf```,功能较少前后台不分离，欢迎使用.

![图片展示](https://github.com/yusan123/sharefile/raw/master/pics/sharefile_error.jpg)

### A designated size of disk space can be used as a shared network disk, and upload and download, delete, package download, batch delete and other functions can be individually set.
### The main technology uses springboot and thymeleaf, with less functions and no separation of front and back. Welcome to use.
## 使用说明
#### 1.源码启动 springboot启动方式
#### 2.编译出包
```shell script
sh script/build.sh
#完成后
ls sharefile-0.0.1-SNAPSHOT-bin.tgz
```
#### 3.运行服务
```shell script
tar zxvf sharefile-0.0.1-SNAPSHOT-bin.tgz
cd sharefile-0.0.1-SNAPSHOT-bin
#修改配置，主要修改file.path即可
vim application.yml
#linux mac启动
sh start.sh
#linux mac停止
sh shutdown.sh

# windowns 启动停止
# 双击启动
start.bat
#停止 关闭窗口
```
