PROC_SEARCH='[s]erver.Main'
IS_AWAKE=`ps -ef | grep $PROC_SEARCH | wc -l`
if [ $IS_AWAKE -ge 1 ]; then
	echo "Killing server.."
	#kill `ps -ef | grep $PROC_SEARCH | awk '{ print $2 }'`
	exit 0;
else
	echo "No server instance found"
fi
