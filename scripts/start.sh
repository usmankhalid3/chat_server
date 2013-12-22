LOG_DIR=~/chat_server/logs
OUT_LOG=$LOG_DIR/out.log
ERR_LOG=$LOG_DIR/err.log
PORT=5555

echo "Compiling..."
javac -d ../../chat_server -sourcepath ../src -cp ../:../libs/guava-11.0.2.jar ../src/server/Main.java
cp -R ../libs/ ../../chat_server/libs/
COMPILED=$?
if [ $COMPILED -ge 1 ]; then
	echo "Failed to compile!!!"
	exit 1
fi
echo "Running server..."
cd "../../chat_server/"
echo "nohup java -cp libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &" >> $OUT_LOG
nohup java -cp libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &