#!/usr/bin/env bash

# query the encrypt status
curl  http:/root:toor@confserver.ailu.internal:9999/admin/user/exists/sta

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\r\n"

# query the encrypt status
curl  http:/root:toor@confserver.ailu.internal:9999/encrypt/status

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl  http:/root:toor@confserver.ailu.internal:9999/encrypt -d asdf

echo "\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
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
curl  http:/root:toor@confserver.ailu.internal:9999/admin/ws/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl  http:/root:toor@confserver.ailu.internal:9999/admin/add/ws/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"