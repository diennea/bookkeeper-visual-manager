
FROM ubuntu:20.04

ARG TARBALL
RUN test -n "$TARBALL"


ENV WEB_PORT=4500
EXPOSE $WEB_PORT
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64

RUN apt-get update \
     && apt-get -y dist-upgrade \
     && apt-get -y install --no-install-recommends openjdk-11-jdk-headless netcat dnsutils less curl unzip \
     && apt-get -y --purge autoremove \
     && apt-get autoclean \
     && apt-get clean \
     && rm -rf /var/lib/apt/lists/*


ADD ${TARBALL} /
RUN mkdir /bkvm
RUN unzip /bkvm-* && cd /bkvm-*/ && mv ./* /bkvm && rm -rf /bkvm-*
COPY entrypoint.sh /bkvm/entrypoint.sh

WORKDIR /bkvm
ENTRYPOINT [ "/bkvm/entrypoint.sh" ]
CMD ["/bkvm/bin/service", "server", "console"]
