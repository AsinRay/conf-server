#!/usr/bin/env bash

# 将ca.cer重新导入到jvm的cacerts中
JAVA_HOME=`java -XshowSettings:properties -version 2>&1 | sed '/^[[:space:]]*java\.home/!d;s/^[[:space:]]*java\.home[[:space:]]*=[[:space:]]*//'`
KS=${JAVA_HOME}/lib/security/cacerts
echo $KS
CA=$1
alias="cnfsrv-ca"
echo $CA will be import into $KS with alias $alias ...

PS=changeit

E=`keytool -list -v -keystore $KS -storepass $PS | grep $alias `
[ "$E""E" = "E" ] || keytool -delete -alias ${alias} -keystore $KS  -storepass $PS
keytool -import -v -noprompt -trustcacerts -alias ${alias} -keystore $KS -storepass $PS -file $CA
keytool -list -v -keystore $KS -storepass $PS | grep $alias