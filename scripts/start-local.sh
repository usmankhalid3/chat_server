#! /bin/bash

DIR=`dirname $0`
source $DIR/stopServer.sh

SRC_DIR=~/Desktop/Projects/chat
DST_DIR=~/Desktop/Projects/chat_server

LOG_DIR=$DST_DIR/logs
OUT_LOG=$LOG_DIR/out.log
ERR_LOG=$LOG_DIR/err.log
PORT=5555

stopServer 0
cd "../.."
echo "Removing $DST_DIR"
rm -rf $DST_DIR
echo "Creating $DST_DIR"
mkdir $DST_DIR
mkdir $LOG_DIR
touch $OUT_LOG
touch $ERR_LOG
echo "Compiling..."
javac -d $DST_DIR -sourcepath $SRC_DIR/src -cp $SRC_DIR:$SRC_DIR/libs/guava-11.0.2.jar $SRC_DIR/src/server/Main.java
cp -R $SRC_DIR/libs/ $DST_DIR/libs/
COMPILED=$?
if [ $COMPILED -ge 1 ]; then
	echo "Failed to compile!!!"
	exit 1
fi
echo "Running server..."
echo "nohup java -cp $DST_DIR:$DST_DIR/libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &" >> $OUT_LOG
nohup java -cp $DST_DIR:$DST_DIR/libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &