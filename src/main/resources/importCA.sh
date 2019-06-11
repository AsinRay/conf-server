KS=$JAVA_HOME/lib/security/cacerts \

keytool -list -v -keystore $KS -storepass changeit | grep cnfsrv-ca
keytool -export -v -alias cnfsrv -keystore cnfsrv.p12 -storepass srv666 -storetype PKCS12 -rfc -file ca.cer
keytool -import -v -noprompt -trustcacerts -alias cnfsrv-ca -file ca.cer -keystore $KS
keytool -list -v -keystore $KS | grep cnfsrv-ca