## 简介
### 可以通过本地磁盘的指定大小的空间，实现共享的网盘，可以上传下载，删除，打包下载，批量删除.

![图片展示](https://github.com/yusan123/sharefile/raw/master/pics/sharefile_error.jpg)

### A shared network disk can be realized through the specified size of the local disk space, which can be uploaded and downloaded, deleted, packaged and downloaded, and batch deleted.
## 使用说明
####1.源码启动 springboot启动方式
####2.编译出包
```shell script
sh script/build.sh
#完成后
ls sharefile-0.0.1-SNAPSHOT-bin.tgz
```
####3.运行服务
```shell script
tar zxvf sharefile-0.0.1-SNAPSHOT-bin.tgz
cd sharefile-0.0.1-SNAPSHOT-bin
#修改配置，主要修改file.path即可
vim application.yml
#启动
sh start.sh
#停止
sh shutdown.sh
```
