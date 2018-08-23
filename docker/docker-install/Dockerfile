FROM openjdk:8-jdk as baseequella

# Install needed tools to install and run openEQUELLA
# Clean up the apt cache afterwards.

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN \
  apt-get update \
  && apt-get install -y \
    curl \
    imagemagick \
    libav-tools \
    unzip \
  && rm -rf /var/lib/apt/lists/*

FROM baseequella as installer

# Install openEQUELLA

# Leave off the .zip at the end.
ENV OEQ_INSTALL_FILE equella-installer-6.7

COPY ["$OEQ_INSTALL_FILE.zip","defaults-flags.xml", "./"]

RUN unzip $OEQ_INSTALL_FILE.zip \
    && java -jar $OEQ_INSTALL_FILE/enterprise-install.jar --unsupported defaults-flags.xml

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
COPY reconfigure.sh ./

# At this point openEQUELLA is installed.

EXPOSE 8080

ARG MEM=512
ARG JVM_ARGS
ENV MEM $MEM
ENV JVM_ARGS $JVM_ARGS
ENV OEQ_ADMIN_DOMAIN addr
ENV OEQ_ADMIN_PORT 0001
ENV OEQ_ADMIN_SUFFIX sa
ENV OEQ_DB_HOST dbhost
ENV OEQ_DB_PORT 0000
ENV OEQ_DB_NAME dbname
ENV OEQ_DB_USERNAME dbuser
ENV OEQ_DB_PASSWORD dbuserpw

CMD sh reconfigure.sh; java -Xmx${MEM}m $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer
