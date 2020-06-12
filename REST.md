# REST API of the config server

## REST API with SSL

```sh
#!/usr/bin/env bash

echo quit | openssl s_client -showcerts -servername cnf.x.com -connect cnf.x.com:8443 > cacert.pem

# Change root password

# admin/pass/{oldpass}/{newpass}
# return true or false
curl --cacert cacert.pem  https://root:toor@cnf.x.com:8443/admin/pass/root/root

# Generate a new token
curl --cacert cacert.pem  https://root:toor@cnf.x.com:8443/admin/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# Assign token to specified repo.

# add/{repo}/{token}
# repo  is the repository and the token is generte by last step /admin/token
curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/admin/add/{repo}/{token}

# Query the token of given repository

# {repo}/token
curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/admin/{repo}/token

# Check if the repository exist.

# {repo}/exist
# Return ture if exists, false otherwise.
curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/admin/{repo}/exist

# Delete the repo token.

# {repo}/del
# Return true if success, false otherwise.

curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/admin/{repo}/del



# query the encrypt status
curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/encrypt/status


# encrypt
curl --cacert cacert.pem  https:/root:toor@cnf.x.com:8443/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
```

## HTTP REST API 

```sh
#!/usr/bin/env bash

# query the encrypt status
curl  http:/root:toor@cnf.x.com:9999/admin/user/exists/sta

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# query the encrypt status
curl  http:/root:toor@cnf.x.com:9999/encrypt/status

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# encrypt
curl  http:/root:toor@cnf.x.com:9999/encrypt -d asdf

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
# change root password

# return false
curl  http:/root:toor@cnf.x.com:9999/admin/pass/root/root

echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# return true
curl  http:/root:toor@cnf.x.com:9999/admin/pass/toor/root
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# return true
curl  http:/root:root@cnf.x.com:9999/admin/pass/root/toor
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"


# query the token of given app
curl  http:/root:toor@cnf.x.com:9999/admin/ot/token
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"

# add new token of given app
curl  http:/root:toor@cnf.x.com:9999/admin/add/ot/token:token@
echo "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
```