#!/bin/bash

set -x

HOME=$(cd "$(dirname $0)/..";pwd)
cd $HOME

#maven package 打包
mvn clean package -Dmaven.skip.test=true

#exp:sharefile-0.0.1-SNAPSHOT.jar
jarname=`ls ./target |grep jar |grep -v original`

# exp:sharefile-0.0.1-SNAPSHOT 获取文件名不包含扩展名
name=${jarname%.*}

#临时目录用来打包
DIR=$name-bin

#清理之前的数据
rm -rf $DIR
rm -rf $DIR.tgz

mkdir -p $DIR

cp ./script/start.* $DIR
cp ./script/shutdown.sh $DIR
cp ./script/application.yml $DIR
cp ./target/*.jar $DIR

chmod -R 766 $DIR/*.sh

dos2unix $DIR/*.sh

tar -zcvf $DIR.tgz $DIR

rm -rf $DIR

echo "build package success!"