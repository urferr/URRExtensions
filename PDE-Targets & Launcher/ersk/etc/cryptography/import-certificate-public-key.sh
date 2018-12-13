#!/bin/bash

read -p"Certificate public key file?(example: riskengine.cer) " publicKeyFile
read -p"Certificate Alias?(example: riskengine) " publicKeyAlias
read -p"Keystore file? (example: riskengine.jks) " serverKeyStoreFile
read -p"Keystore Password? " serverKeyStorePassword

keytool -import -keystore $serverKeyStoreFile -storepass $serverKeyStorePassword -alias $publicKeyAlias -file $publicKeyFile -noprompt
