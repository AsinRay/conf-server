# 安全问题

## server SSL 访问的安全

### 为Http Server生成SSL Key

Note: 生成jks或者p12格式的证书存储时请注意其中的O=confserver.xxx.internal这个参数需要设置为客户端请求的host。

```sh
#!/usr/bin/env bash

# 生成jks格式
# 使用RSA加密，生成一个有效期为１年,别名为cnfsrv,密码为keypassAsin,存储密码为srv666的server.jks.
keytool -validity 365 -genkey -v -alias cnfsrv -keyalg RSA -keystore server.jks -keypass keypassAsin  -storepass srv666 -dname "CN=Web Server,OU=China,O=confserver.xxx.internal,L=Beijing,S=Beijing,C=China"

# 生成p12格式
keytool -genkeypair -alias cnfsrv -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore cnfsrv.p12 -validity 3650 -keypass srv666 -storepass srv666 -dname "CN=confserver.xxx.internal,OU=China,O=confserver.xxx.internal,L=Beijing,S=Beijing,C=China"

# 导出证书
keytool -export -alias cnfsrv -file cnfsrv.cer -keystore cnfsrv.p12 -storepass srv666
```

### 生成客户端证书

生成客户端证书时，keytool参数O=client.xxx.internal 没有强制性要求。

```sh
keytool -validity 365 -genkeypair -v -alias cnfcli -keyalg RSA -storetype PKCS12 -keystore client.p12 -keypass cli666  -storepass cli666 -dname "CN=client,OU=China,O=client.xxx.internal,L=Beijing,S=Beijing,C=China"
```

### 客户端证书加入到服务器证书信任

由于是双向SSL认证，服务器必须要信任客户端的证书，因此，必须将客户端证书添加为服务器的信任认证列表，由于不能直接将PKCS12格式的证书库导入，
我们必须先把客户端证书导出为一个单独的CER格式的文件，使用如下命令:

```sh
keytool -export -v -alias cnfcli -keystore client.p12 -storetype PKCS12 -storepass cli666 -rfc -file cnfcli.cer
keytool -import -v -alias cnfcli -file cnfcli.cer -keystore cnfsrv.p12 -storepass srv666 -noprompt
```

### 服务器证书加入客户端信任

```sh
keytool -export -v -alias cnfsrv -keystore cnfsrv.p12 -storepass srv666 -rfc -file cnfsrv.cer
keytool -import -v -alias cnfsrv -file cnfsrv.cer -keystore client.p12 -storepass cli666 -noprompt
```

### 检查证书是否正确导入

```sh
# 删除中间生成的cer文件
rm *.cer
# 查看一下client的证书是否正确的导入server.jks
keytool -list -v -keystore cnfsrv.p12 -storepass srv666
# 检查server的证书是否正确地导入到client.p12中
keytool -list -v -keystore client.p12 -storepass cli666
```

### 配置证书文件到 http server

请参照resources/config/applicaton.properties文件

```properites

spring.profiles.active=pro
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:cnfsrv.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=srv666
server.ssl.key-password=srv666
server.ssl.key-alias=cnfsrv
server.ssl.protocol=TLSv1.2
server.use-forward-headers=true
server.http2.enabled=true

server.undertow.worker-threads=8
server.undertow.buffer-size=1024
server.undertow.direct-buffers=true
server.undertow.io-threads=4

management.endpoint.mappings.enabled=true
management.endpoints.web.exposure.include=*
management.server.port=8443
management.server.servlet.context-path=/admin

persistence.filepath=/user/

```

### 导入证书到客户端JVM

导入证书到客户端的JVM中，只需要将之前生成的cnfcli.p12导出成ca.cer证书，然后直接导入到JVM中。

```sh
#!/usr/bin/env bash
java -XshowSettings:properties -version

#jdk 8
KS=$JAVA_HOME/jre/lib/security/cacerts
#jdk 11
KS=$JAVA_HOME/lib/security/cacerts

keytool -list -keystore $KS -storepass changeit | grep cnfsrv


keytool -export -v -alias cnfcli -keystore client.p12 -storetype PKCS12 -storepass cli666 -rfc -file ca.cer
keytool -import -noprompt -trustcacerts -alias cnfsrv-ca -file ca.cer -keystore $KS
keytool -list -v -keystore $KS | grep cnfsrv-ca
```

### 更新导入JVM的证书

```sh
#!/usr/bin/env bash
# 查看jvm相关信息
java -XshowSettings:properties -version

#jdk 8 的证书路径
KS=$JAVA_HOME/jre/lib/security/cacerts

#jdk 11 的证书路经
KS=$JAVA_HOME/lib/security/cacerts


# 查看是否存在别名为cnfsrv的证书
keytool -list -keystore $KS -storepass changeit | grep cnfsrv

# 删除cacerts中指定名称的证书(需要root权限)
sudo keytool -delete -alias cnfsrv -keystore $KS  -storepass changeit

# 导入指定证书到cacerts：
keytool -import　-trustcacerts -alias cnfsrv -file cnfsrv.cer -keystore $KS  -storepass changeit
```

## 加密的安全

这部分的安全可以配置在application.properties 文件中，也可以单独配置，为了表明config server也可以有自己的bootstrap.yml配置文件，
我们将加密部分的安全配置在了这个文件中,配置的路径为classpath*:encrypt.conf-svc.jks。

```yml
encrypt:
  fail-on-error: false
  key-store:
    alias: asin
    location: classpath*:encrypt.jks
    # 必选参数，keytools中的 -storepass
    password: ${KEYSTORE_PASSWORD:asinRay666}
    # 可选参数, keytools中的 -keypass 此参数在生成jks时会被忽略
    secret: asinRay666
```

可以看到，我们使用了一个keystore.jks的文件来存储相关信息。

### keystore.jks 的生成

```sh
#!/usr/bin/env bash
keytool -genkeypair -alias asin -keyalg RSA -dname "CN=Web Server,OU=China,O=www.a.com,L=Beijing,S=Beijing,C=China" -keypass asinRay666 -keystore encrypt.jks -storepass asinRay666
```

Note:
如果您使用的是jdk11的keytool生成的jks文件，在生成时会有如下警告

Warning:  Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified -keypass value.

那么请将-keypss 和 -storepass 设置成相同即可，在yml配置时也配置相同就行了。

如果您想使用不同的-keypass和-storepass，请使用jdk8版本的keytool生成jks,然后将其应用于jdk11环境下,也可使用p12格式来实现。