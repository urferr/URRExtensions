#!/bin/bash

# Set the values we'll use for the generation
read -p"Certificate Alias?(example: riskengine) " certificateAlias
read -p"Certificate distiguished name(DN)?(example: CN=commonName, OU=orgUnit, O=org, L=city, S=state, C=countryCode) " distinguishedName
read -p"Certificate Password? " certificatePassword
read -p"Certificate Keystore Password? " keyStorePassword

targetFolder="./"
keyStoreFileName="$targetFolder/$certificateAlias-keystore.jks"
certificateFileName="$targetFolder/$certificateAlias-public-key.cer"

mkdir -p $targetFolder

rm -f $keyStoreFileName
#keytool -genkey -alias $certificateAlias -keyalg RSA -sigalg SHA1withRSA -keypass $certificatePassword -storepass $keyStorePassword -keystore $keyStoreFileName -dname $distinguishedName -validity 3600
keytool -genkeypair -alias $certificateAlias -keypass $certificatePassword -storepass $keyStorePassword -keystore $keyStoreFileName -storetype jks -validity 3650 -keysize 2048 -keyalg RSA -sigalg SHA1withRSA -dname $distinguishedName
echo "Created: $keyStoreFileName"

rm -f $certificateFileName
keytool -export -rfc -keystore $keyStoreFileName -storepass $keyStorePassword -alias $certificateAlias -file $certificateFileName
echo "Created: $certificateFileName"
