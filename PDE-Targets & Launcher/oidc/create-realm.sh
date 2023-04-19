#!/bin/bash

podman exec --env SPRING_CLOUD_CONFIG_LABEL=${1} infra_keycloak_1 bash -ec 'java -Xmx256m -jar /opt/adorsys/keycloak-config-cli.jar \
    --debug=false \
	--keycloak.url=http://pdvmdev15:10040/auth \
	--keycloak.user=admin \
	--keycloak.password=admin \
	--import.files.locations=${SPRING_CLOUD_CONFIG_URI:-http://config:8080}/keycloak/${SPRING_PROFILES_ACTIVE:-default}/${SPRING_CLOUD_CONFIG_LABEL:-master}/keycloak/xentis-realm.json'