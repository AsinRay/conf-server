# REST API Function

## 请求的endpoint prefix

<https://yourHost:yourPort/>

## 功能与请求

## 修改admin默认的密码

admin/{oldPassword}/{newPassword}

```sh
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/{oldPassword}/{newPassword}
```

## 生成一个token

使用如下请求生成一个token.

admin/token

```sh
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/token
```

## 将生成的token分配给指定的应用


```sh

```

```sh
#!/usr/bin/env bash

echo quit | openssl s_client -showcerts -servername confserver.xxx.internal -connect confserver.xxx.internal:8443 > cacert.pem

# query the encrypt status
curl --cacert cacert.pem https:/root:toor@confserver.xxx.internal:8443/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl --cacert cacert.pem  https:/root:root@confserver.xxx.internal:8443/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# query the token of given app
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/ot/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl --cacert cacert.pem  https:/root:toor@confserver.xxx.internal:8443/admin/add/ot/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

```

## REST API without SSL

```sh
#!/usr/bin/env bash

# query the encrypt status
curl  http:/root:toor@confserver.xxx.internal:9999/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl  http:/root:toor@confserver.xxx.internal:9999/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl  http:/root:toor@confserver.xxx.internal:9999/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl  http:/root:toor@confserver.xxx.internal:9999/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl  http:/root:toor@confserver.xxx.internal:9999/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl  http:/root:root@confserver.xxx.internal:9999/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# query the token of given app
curl  http:/root:toor@confserver.xxx.internal:9999/admin/ot/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl  http:/root:toor@confserver.xxx.internal:9999/admin/add/ot/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
```