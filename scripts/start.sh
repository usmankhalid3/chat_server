LOG_DIR=~/chat_server/logs
OUT_LOG=$LOG_DIR/out.log
ERR_LOG=$LOG_DIR/err.log
PORT = 5555

echo "Compiling..."
COMPILED = `javac -d ../../chat_server -sourcepath src -cp ../:../libs/guava-11.0.2.jar ../src/server/Main.java`
if [ $COMPILED -ge 1 ]; then
	echo "Failed to compile!!!"
	exit 1
fi
echo "Running server..."
echo "nohup java -cp ../../chat_server/:../libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &" >> $OUT_LOG
nohup java -cp ../chat_server/:../libs/guava-11.0.2.jar server.Main $PORT 1>> $OUT_LOG 2>> $ERR_LOG &