#!/usr/bin/env bash

KS=$JAVA_HOME/lib/security/cacerts
alias="cnfsrv-ca"

keytool -list -v -keystore $KS -storepass changeit | grep ${alias}

keytool -delete -alias ${alias} -keystore $KS  -storepass changeit

keytool -import -v -noprompt -trustcacerts -alias ${alias} -file ca.cer -keystore $KS -storepass changeit

keytool -list -v -keystore $KS -storepass changeit | grep ${alias}
