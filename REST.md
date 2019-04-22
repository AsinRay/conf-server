# REST API of the config server

## REST API with SSL

```sh
#!/usr/bin/env bash

echo quit | openssl s_client -showcerts -servername confserver.ailu.internal -connect confserver.ailu.internal:8443 > cacert.pem

# query the encrypt status
curl --cacert cacert.pem https:/root:toor@confserver.ailu.internal:8443/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl --cacert cacert.pem  https:/root:root@confserver.ailu.internal:8443/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# query the token of given app
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/admin/ot/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl --cacert cacert.pem  https:/root:toor@confserver.ailu.internal:8443/admin/add/ot/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

```

## REST API without SSL

```sh
#!/usr/bin/env bash

# query the encrypt status
curl  http:/root:toor@confserver.ailu.internal:9999/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl  http:/root:toor@confserver.ailu.internal:9999/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl  http:/root:toor@confserver.ailu.internal:9999/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl  http:/root:toor@confserver.ailu.internal:9999/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl  http:/root:toor@confserver.ailu.internal:9999/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl  http:/root:root@confserver.ailu.internal:9999/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# query the token of given app
curl  http:/root:toor@confserver.ailu.internal:9999/admin/ot/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl  http:/root:toor@confserver.ailu.internal:9999/admin/add/ot/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
```