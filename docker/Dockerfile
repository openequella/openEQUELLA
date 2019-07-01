FROM openjdk@sha256:0e25c8428a56e32861fe996b528a107933155c98fb2a9998a4a4e9423aad734d as baseequella

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

ARG OEQ_INSTALL_FILE=equella-installer-2019.1.zip

COPY ["$OEQ_INSTALL_FILE","defaults.xml", "./"]

RUN unzip $OEQ_INSTALL_FILE -d /tmp \
    && mv /tmp/equella-installer-* /equella-installer \
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

# At this point openEQUELLA is installed.

EXPOSE 8080

ARG MEM=512
ARG JVM_ARGS
ENV MEM $MEM
ENV JVM_ARGS $JVM_ARGS

# Properties in the optional-config, mandatory-config, and hibernate files can be 
# overridden by oEQ logic via environment variables by changing '.' to '_', 
# uppercasing, and prepending "EQ_" to the name.

ARG EQ_HTTP_PORT=8080
ENV EQ_HTTP_PORT $EQ_HTTP_PORT

ARG EQ_ADMIN_URL=http://localhost:8080/admin/
ENV EQ_ADMIN_URL $EQ_ADMIN_URL

ARG EQ_HIBERNATE_CONNECTION_URL=jdbc:postgresql://localhost:5432/equella
ENV EQ_HIBERNATE_CONNECTION_URL $EQ_HIBERNATE_CONNECTION_URL

ARG EQ_HIBERNATE_CONNECTION_USERNAME=equellauser
ENV EQ_HIBERNATE_CONNECTION_USERNAME $EQ_HIBERNATE_CONNECTION_USERNAME

ARG EQ_HIBERNATE_CONNECTION_PASSWORD=password
ENV EQ_HIBERNATE_CONNECTION_PASSWORD $EQ_HIBERNATE_CONNECTION_PASSWORD

CMD java -Xmx${MEM}m $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer
