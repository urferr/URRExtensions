#!/bin/bash

podman run --name import-xentis --rm \
	-e KEYCLOAK_URL=http://pdvmdev15.profidatagroup.com:10040/auth \
	-e KEYCLOAK_USER=admin \
	-e KEYCLOAK_PASSWORD=admin \
	-e WAIT_TIME_IN_SECONDS=120 \
	-e IMPORT_PATH=/config/config.json \
	-e IMPORT_VARSUBSTITUTION_ENABLED=true \
	-e IMPORT_FORCE=true \
	-e XENTIS_REALM=$1 \
	-e XENTIS_BASE_URL=http://pdvmdev15.profidatagroup.com:$2/xentis \
	-e XENTIS_EXTERNAL_URL=http://pdvmdev15.profidatagroup.com:$3 \
	-v $PWD/xentis-realm-template.json:/config/config.json \
	adorsys/keycloak-config-cli:latest-18.0.0