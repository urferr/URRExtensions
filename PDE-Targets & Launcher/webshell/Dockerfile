FROM docker.io/tsl0922/ttyd

RUN apt-get update && \
    apt-get install -y wget && \
    apt-get clean && \
    wget -O - https://github.com/containers/podman/releases/download/v4.4.0/podman-remote-static-linux_amd64.tar.gz | tar -C /usr/local/bin -xzf - && \
    mv /usr/local/bin/podman-remote-static-linux_amd64 /usr/local/bin/podman-remote
	
ENV CONTAINER_HOST="unix:///run/podman/podman.sock"