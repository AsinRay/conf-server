#!/usr/bin/env bash

APP_NAME=@project.name@
BUILD_JAR=@project.build.finalName@.jar


JAVA=`which java`
UNAME=`which uname`
GREP=`which egrep`
CUT=`which cut`
READLINK=`which readlink`
XARGS=`which xargs`
DIRNAME=`which dirname`
MKTEMP=`which mktemp`
RM=`which rm`
CAT=`which cat`
SED=`which sed`

if [ -z ${JAVA} ]; then
    echo "java is missing - check beginning of \"$0\" file for details."
    exit 1
fi

if [ -z "$UNAME" -o -z "$GREP" -o -z "$CUT" -o -z "$MKTEMP" -o -z "$RM" -o -z "$CAT" -o -z "$SED" ]; then
    xmessage "Required tools are missing - check beginning of \"$0\" file for details."
    exit 1
fi

# OS_TYPE=`"$UNAME" -s`

JAVA_HOME=`${JAVA} -XshowSettings:properties -version 2>&1 | sed '/^[[:space:]]*java\.home/!d;s/^[[:space:]]*java\.home[[:space:]]*=[[:space:]]*//'`
CLZ_VERSION=`${JAVA} -XshowSettings:properties -version 2>&1 | sed '/java.class.version/!d' | awk '{print $3}'`
GE_JDK8=52.0

if [ ${CLZ_VERSION} \< ${GE_JDK8} ]; then
    echo "App need JDK8 or later version. please run java --version to check the JDK version."
    exit 1
fi

# bin
BIN_PATH=$(cd `${DIRNAME} $0`; pwd)
APP_HOME=$(${DIRNAME} ${BIN_PATH})
PID_FILE=${APP_HOME}"/pid"
CMD=java

LOG_DIR=${APP_HOME}"/logs/"
START_LOG=${LOG_DIR}"/start.log"
HUP_LOG=${LOG_DIR}"/nohup.log"

#USR=user


mklog(){
if [ ! -d $1 ]; then
    mkdir -p $1
fi
}

getOption(){
OPTIONS_FILE=${APP_HOME}"/bin/"$1
if [ -r ${OPTIONS_FILE} ]; then
    OPTION=" "`${CAT} ${OPTIONS_FILE} | ${GREP} -v "^#.*" | ${GREP} -v "^$" `
else
    echo "Waring: Can't find option file: ${OPTIONS_FILE}."
fi
}
## For spring boot BOOT-INF only
# OPTS=" -Dloader.path=${APP_HOME}/lib,${APP_HOME}/config "
OPTIONS='app.options jvm.options'
for f in $OPTIONS; do
    getOption $f
    OPTS=${OPTS}${OPTION}
done

# GC log
log=`pwd`"/logs/"

mklog $log
mklog $LOG_DIR

start(){
    if [ -f "$PID_FILE" ] ;then
        echo
        echo  echo "Already started. PID: [$( cat $PID )]"
    else
        echo "==== Start"
        # Lock file that indicates that no 2nd instance should be started
        touch $PID_FILE
        # COMMAND is called as background process and ignores SIGHUP signal, writes it's
        # output to the LOG file.
        COMMAND=$CMD ${OPTS} -jar ${APP_HOME}/boot/${BUILD_JAR}
        if nohup $COMMAND >>$HUP_LOG 2>&1 &
        # The pid of the last background is saved in the PID file
        then
            pid=$!
            echo ${pid} >$PID_FILE
            echo "$(date '+%Y-%m-%d %X'): [INFO] App started :" >> $START_LOG
            echo "            - App Home: ${APP_HOME}" >> $START_LOG
            echo "            - App pid:  ${pid}" >> $START_LOG
            echo "App started, pid: ${pid}"
        else
            echo "Error... "
            /bin/rm $PID_FILE
        fi
    fi
}

#echo "nohup java ${OPTS} -jar ${APP_HOME}/boot/${BUILD_JAR} > nohubp.out 2>&1 & "
#nohup java ${OPTS} -jar ${APP_HOME}/boot/${BUILD_JAR} > nohup.out 2>&1 &



case "$1" in

    'start')
        start
            ;;
    'stop')
        stop
            ;;
    'restart')
        stop ; echo "Sleeping..."; sleep 1 ;
        start
            ;;
    'status')
        status
            ;;
     *)
    echo
    echo "Usage: $0 { start | stop | restart | status }"
    echo
    exit 1
    ;;
esac
exit 0
