#! /bin/bash

function stopServer {
	COMPLAIN=$1
	PROC_SEARCH='[s]erver.Main'
	IS_AWAKE=`ps -ef | grep $PROC_SEARCH | wc -l`
	if [ $IS_AWAKE -ge 1 ]; then
		echo "Killing server..."
		kill `ps -ef | grep $PROC_SEARCH | awk '{ print $2 }'`
	else
		if [ $COMPLAIN -gt 0 ]; then
			echo "No server instance found"
		fi
	fi
}