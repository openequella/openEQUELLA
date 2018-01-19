FROM openjdk:8-jdk as baseequella

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN \
  apt-get update \
  && apt-get install -y libav-tools imagemagick curl unzip

FROM baseequella as installer

COPY ["equella-installer-6.6.zip","defaults.xml", "./"]

RUN unzip equella-installer-6.6.zip \
    && java -jar equella-installer-6.6/enterprise-install.jar --unsupported defaults.xml

FROM baseequella as equella

RUN useradd -ms /bin/bash equella
WORKDIR /home/equella
COPY --from=installer /home/equella/equella equella
RUN mkdir -p /home/equella/equella/filestore/ \
    && mkdir -p /home/equella/equella/freetext/ \
    && chown -R equella:equella equella
WORKDIR /home/equella/equella
USER equella
VOLUME ["/home/equella/equella/filestore/", "/home/equella/equella/freetext/"]

COPY learningedge-log4j.properties learningedge-config/

EXPOSE 8080

ARG MEM=512
ARG JVM_ARGS
ENV MEM $MEM
ENV JVM_ARGS $JVM_ARGS

CMD java -Xmx${MEM}m $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer
