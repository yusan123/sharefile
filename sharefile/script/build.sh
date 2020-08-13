#!/bin/bash
set -x
HOME=$(cd "$(dirname $0)/..";pwd)
cd $HOME

DIR=sharefile-bin

rm -rf $DIR
rm -rf sharefile.tgz

mkdir -p $DIR

mvn clean package -Dmaven.skip.test=true

cp ./script/start.sh $DIR
cp ./script/shutdown.sh $DIR
cp ./script/application.yml $DIR

chmod -R 766 $DIR/*.sh

dos2unix $DIR/*.sh

ls -l $DIR

cp ./target/*.jar $DIR

tar -zcvf sharefile.tgz $DIR

rm -rf $DIR