#!/usr/bin/env bash
# 将ca.cer重新导入到client.p12 中
JAVA_HOME=`java -XshowSettings:properties -version 2>&1 | sed '/^[[:space:]]*java\.home/!d;s/^[[:space:]]*java\.home[[:space:]]*=[[:space:]]*//'`

KS=${JAVA_HOME}/lib/security/cacerts
echo $KS
# keytool -list -v  -keystore $KS -storepass changeit
#echo ">>>>>>>>>>>>>>>>>>>>>>"
alias="cnfsrv-ca"
keytool -list -v  -keystore $KS -storepass changeit | grep -C 10 ${alias}