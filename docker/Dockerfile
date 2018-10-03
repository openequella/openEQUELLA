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
ARG OEQ_INSTALL_FILE
ARG OEQ_INSTALL_ZIP_ROOT_DIR

COPY ["$OEQ_INSTALL_FILE.zip","defaults.xml", "./"]

RUN unzip $OEQ_INSTALL_FILE.zip \
    && mv $OEQ_INSTALL_ZIP_ROOT_DIR /equella-installer \
    && java -jar /equella-installer/enterprise-install.jar --unsupported defaults.xml

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
ENV OEQ_ADMIN_DOMAIN localhost
ENV OEQ_ADMIN_PORT 8080
ENV OEQ_ADMIN_SUFFIX admin/
ENV OEQ_DB_HOST db
ENV OEQ_DB_PORT 5432
ENV OEQ_DB_NAME equella
ENV OEQ_DB_USERNAME equellauser
ENV OEQ_DB_PASSWORD password

CMD sh reconfigure.sh; java -Xmx${MEM}m $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer
