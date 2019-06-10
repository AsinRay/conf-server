#!/usr/bin/env bash
basepath=$(cd `dirname $0`; pwd)


sec_arr=("cnfsrv.p12" "client.p12" "encrypt.jks" "cnfsrv.cer")
#echo "elements: ${sec_arr[*]}"
#echo "elements length ${#sec_arr[*]}"

echo ">>>>>>>>>>>>>>>"

for i in ${sec_arr[@]}; do
    f=${basepath}/${i}
    if [[ -f "$f" ]]; then
        rm ${f}
    fi
done
echo ">>>>>>>>>>>>>>>"



read -p "Enter your host, please:" host
read -p "valid period days:" days

echo "$host is the host."
echo "$days is the valid period days."

# 生成Server p12格式证书
keytool -genkeypair -alias cnfsrv -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore cnfsrv.p12 -validity $days -keypass srv666 -storepass srv666 -dname "CN=$host,OU=China,O=$host,L=Beijing,S=Beijing,C=China"
# 导出证书
keytool -export -v -alias cnfsrv -keystore cnfsrv.p12 -storepass srv666 -rfc -file cnfsrv.cer

# 生成客户证书
keytool -validity 365 -genkeypair -v -alias cnfcli -keyalg RSA -storetype PKCS12 -keystore client.p12 -keypass cli666  -storepass cli666 -dname "CN=client,OU=China,O=$host,L=Beijing,S=Beijing,C=China"
keytool -export -v -alias cnfcli -keystore client.p12 -storetype PKCS12 -storepass cli666 -rfc -file cnfcli.cer

keytool -import -v -alias cnfcli -file cnfcli.cer -keystore cnfsrv.p12 -storepass srv666 -noprompt
keytool -import -v -alias cnfsrv -file cnfsrv.cer -keystore client.p12 -storepass cli666 -noprompt