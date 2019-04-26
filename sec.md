# 安全问题

## server SSL 访问的安全

### 为Http Server生成SSL Key

使用RSA加密，生成一个有效期为１年,别名为cnfsrv,密码为keypassAsin,存储密码为srv666的server.jks.

```sh
#!/usr/bin/env bash

# 生成jks格式
keytool -validity 365 -genkey -v -alias cnfsrv -keyalg RSA -keystore server.jks -keypass keypassAsin  -storepass srv666 -dname "CN=Web Server,OU=China,O=www.aliyun.com,L=Beijing,S=Beijing,C=China"

# 生成p12格式
keytool -genkeypair -alias cnfsrv -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore cnfsrv.p12 -validity 3650 -keypass srv666 -storepass srv666 -dname "CN=confserver.ailu.internal,OU=China,O=ailu.internal,L=Beijing,S=Beijing,C=China"

# 导出证书
keytool -export -alias cnfsrv -file cnfcli.cer -keystore cnfsrv.p12 -storepass srv666
```

### 生成客户端证书

```sh
keytool -validity 365 -genkeypair -v -alias cnfcli -keyalg RSA -storetype PKCS12 -keystore client.p12 -keypass cli666  -storepass cli666 -dname "CN=client,OU=China,O=www.aliyun.com,L=Beijing,S=Beijing,C=China"
```

### 客户端证书加入到服务器证书信任

由于是双向SSL认证，服务器必须要信任客户端的证书，因此，必须反客户端证书添加为服务器的信任认证列表，由于不能直接将PKCS12格式的证书库导入，我们必须先把客户端证书导出为一个单独的CER格式的文件，使用如下命令:

```sh
keytool -export -v -alias cnfcli -keystore client.p12 -storetype PKCS12 -storepass cli666 -rfc -file client.cer
keytool -import -v -alias cnfcli -file client.cer -keystore server.jks -storepass srv666 -noprompt
```

### 服务器证书加入客户端信任

```sh
keytool -export -v -alias cnfsrv -keystore server.jks -storepass srv666 -rfc -file server.cer
keytool -import -v -alias cnfsrv -file server.cer -keystore client.p12 -storepass cli666 -noprompt
```

### 检查证书是否正确导入

```sh
# 删除中间生成的cer文件
rm *.cer
# 查看一下client的证书是否正确的导入server.jks
keytool -list -v -keystore server.jks -storepass srv666
# 检查server的证书是否正确地导入到client.p12中
keytool -list -v -keystore client.p12 -storepass cli666
```

### 配置证书文件到 http server

### 导入证书到客户端JVM

导入证书到客户端的JVM中，只需要将Server.jks导出成sever.cer证书，然后直接导入到JVM中。

```sh
#!/usr/bin/env bash
java -XshowSettings:properties -version

#jdk 8
KS=$JAVA_HOME/jre/lib/security/cacerts
#jdk 11
KS=$JAVA_HOME/lib/security/cacerts

keytool -list -keystore $KS -storepass changeit | grep cnfsrv


keytool -export -v -alias cnfcli -keystore client.p12 -storetype PKCS12 -storepass cli666 -rfc -file client.cer
keytool -import -noprompt -trustcacerts -alias cnfsrv -file cnfcli.cer -keystore $KS
keytool -list -v -keystore $KS | grep cnfsrv
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

这部分的安全可以配置在application.properties 文件中，也可以单独配置，为了表明config server也可以有自己的bootstrap.yml配置文件，我们将加密部分的安全配置在了这个文件中,配置的路径为classpath*:encrypt.conf-svc.jks。

```yml
encrypt:
  fail-on-error: false
  key-store:
    alias: asin
    location: classpath*:encrypt.jks
    password: ${KEYSTORE_PASSWORD:asinRay666}
    # 可选参数,若配置必须和password一致
    secret: asinRay666
```

可以看到，我们使用了一个keystore.jks的文件来存储相关信息。

### keystore.jks 的生成

```sh
#!/usr/bin/env bash
keytool -genkeypair -alias asin -keyalg RSA -dname "CN=Web Server,OU=China,O=www.a.com,L=Beijing,S=Beijing,C=China" -keypass asinRay666 -keystore encrypt.jks -storepass asinRay666
```