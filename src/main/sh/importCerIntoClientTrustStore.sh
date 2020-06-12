#!/usr/bin/env bash
# 将ca.cer重新导入到client.p12 中
CA=$1
alias=$2
KS=$3
PS=$4

echo $CA will be import into $KS with alias $alias ...


# alias="cnfsrv-ca"
# alias="cnfcli"
#PS=cli666

E=`keytool -list -v -keystore $KS -storepass $PS | grep $alias `
[ "$E""E" = "E" ] || keytool -delete -alias ${alias} -keystore $KS  -storepass $PS
keytool -import -v -noprompt -trustcacerts -alias ${alias} -keystore $KS -storepass $PS -file $CA
keytool -list -v -keystore $KS -storepass $PS | grep $alias