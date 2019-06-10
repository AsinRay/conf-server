#!/usr/bin/env bash


keytool -list -v -keystore cnfsrv.p12 -storepass srv666 | grep cnfcli
keytool -list -v -keystore client.p12 -storepass cli666 | grep cnfsrv