#!/bin/bash

podman run --name import-xentis --rm \
	-e KEYCLOAK_URL=http://pdvmdev15:10040/auth \
	-e KEYCLOAK_USER=admin \
	-e KEYCLOAK_PASSWORD=admin \
	-e WAIT_TIME_IN_SECONDS=120 \
	-e IMPORT_FILES_LOCATIONS=/config/config.json \
	-e IMPORT_VARSUBSTITUTION_ENABLED=true \
	-e IMPORT_FORCE=true \
	-e XENTIS_REALM=$1 \
	-e XENTIS_BASE_URL=http://pdvmdev15.profidatagroup.com:$2/xentis \
	-e XENTIS_EXTERNAL_URL=http://pdvmdev15.profidatagroup.com:$3 \
	-e XENTIS_FRONTEND_URL=http://pdvmdev15.profidatagroup.com:$3/auth \
	-v $PWD/xentis-realm-template.json:/config/config.json \
	adorsys/keycloak-config-cli:5.5.0-20.0.1