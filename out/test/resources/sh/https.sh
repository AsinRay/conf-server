# REST API of the config server

## REST API with SSL


#!/usr/bin/env bash

echo quit | openssl s_client -showcerts -servername confserver.xxx.internal -connect confserver.xxx.internal:8443 > cacert.pem

# query the encrypt status
curl --cacert cacert.pem https://root:toor@confserver.xxx.internal:8443/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl --cacert cacert.pem  https://root:root@confserver.xxx.internal:8443/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# generate new token 
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/admin/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/admin/add/ws/boot:boot@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the token of given app
curl --cacert cacert.pem  https://root:toor@confserver.xxx.internal:8443/admin/ws/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

curl --cacert cacert.pem https://boot:boot@confserver.xxx.internal:8443/ws/dev