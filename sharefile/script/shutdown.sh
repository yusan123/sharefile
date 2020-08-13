#!/bin/bash

pid=`ps -ef |grep sharefile |grep -v grep|awk '{print($2)}'`

kill -9 $pid

