#!/usr/bin/env bash
ca=$1
# 将ca.cer重新导入到cacerts 中, 这需要使用root权限
JAVA_HOME=`java -XshowSettings:properties -version 2>&1 | sed '/^[[:space:]]*java\.home/!d;s/^[[:space:]]*java\.home[[:space:]]*=[[:space:]]*//'`

KS=${JAVA_HOME}/lib/security/cacerts
PS=changeit
alias="cnfsrv-ca"

E=`keytool -list -v -keystore $KS -storepass $PS | grep $alias `
[ "$E""E" = "E" ] || keytool -delete -alias ${alias} -keystore $KS  -storepass $PS

# keytool -list -v -keystore $KS -storepass changeit | grep ${alias}
# keytool -delete -alias ${alias} -keystore $KS  -storepass changeit

keytool -import -v -noprompt -trustcacerts -alias ${alias} -file $ca -keystore $KS -storepass changeit

keytool -list -v -keystore $KS -storepass changeit | grep ${alias}