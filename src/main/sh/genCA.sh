#!/usr/bin/env bash
# 0. prepare env

echo "Prepare env: rebuild target folder."
rm -rf ca
[ -d ca ] || mkdir -p ca

# 1. Generate config server .p12 and export it to cnfsrv.cer
CN="cnfsrv.bittx.org"
OU="China"
O=$CN
L="Beijing"
S="Beijing"
C="China"
DN="CN=$CN,OU=$OU,O=$O,L=$L,S=$S,C=$C"
echo $DN

PASW="srv666"
echo "Generate .p12 format."
echo "Default password is $PASW"
keytool -genkeypair -alias cnfsrv -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ca/cnfsrv.p12 -validity 3650 -keypass $PASW -storepass $PASW -dname $DN
echo "export to .cer format."
keytool -export -v -alias cnfsrv -keystore ca/cnfsrv.p12 -storepass srv666 -rfc -file ca/cnfsrv.cer
echo "export OK"

# 2.  Generate client.p12 and export to client.cer
echo "Generate client.cer."
cliPW=cli666
keytool -validity 365 -genkeypair -v -alias cnfcli -keyalg RSA -storetype PKCS12 -keystore ca/client.p12 -keypass $cliPW -storepass $cliPW -dname "CN=client,OU=China,O=cli.bittx.org,L=Beijing,S=Beijing,C=China"

# 3.  Generate encrypt.jks for spring cloud conf server

echo "Generate encrypt.jks for conf server"
keytool -genkeypair -alias asin -keyalg RSA -dname $DN -keypass AsinRay666 -keystore ca/encrypt.jks -storepass AsinRay666

echo "import cnfsrv into client.p12"
sh ./importCerIntoClientTrustStore.sh ca/cnfsrv.cer cnfsrv ca/client.p12 cli666

echo "Copy server .p12 and encrypt.jks to resource folder."

cp ca/cnfsrv.p12 ca/encrypt.jks ../resources/

# rm -rf ca
