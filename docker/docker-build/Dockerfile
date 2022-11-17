# A bare image ready for building oEQ and also running test suites

FROM openjdk:8 as basebuild

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN \
  apt-get update \
  && apt-get install -y \
    build-essential \
    vim \
    less \
    iproute2 \
# libtinfo5 is needed for Purescript tools
    libtinfo5 \
    imagemagick \
    ffmpeg \
    libimage-exiftool-perl \
# tidy up APT and save some space
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

FROM basebuild as basebuild-testtools

ARG DIR_BIN=/usr/local/bin
ARG GOOGLE_DEB_FILENAME=google-chrome-stable_current_amd64.deb

# Install tools to support running autotests
COPY install-chromedriver $DIR_BIN
RUN \
  apt-get update \
# Install Google Chrome
  && wget -nv https://dl.google.com/linux/direct/$GOOGLE_DEB_FILENAME \
  && apt-get install -y ./$GOOGLE_DEB_FILENAME \
  && rm $GOOGLE_DEB_FILENAME \
  && install-chromedriver $DIR_BIN \
# Install a local PostgreSQL \
  && apt-get install -y postgresql \
  && service postgresql start \
  && su postgres -c "psql -c \"CREATE USER equellatests WITH PASSWORD 'password';\"" \
  && su postgres -c "psql -c \"CREATE DATABASE equellatests WITH OWNER = equellatests;\"" \
  && service postgresql stop \
# tidy up APT and save some space
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

FROM basebuild-testtools as buildenv

ARG CLONEDIR=openequella

RUN \
# Use NVM for managing node versions
  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash \
  && export NVM_DIR="$HOME/.nvm" \
  && . "$NVM_DIR/nvm.sh" \
# An initial clone also provides access to the .nvmrc to setup node
  && git clone https://github.com/openequella/openEQUELLA.git $CLONEDIR \
  && cd $CLONEDIR \
# Establish initial version of Node/NPM, may still want to update later when using image
  && nvm install

COPY entrypoint-buildenv /usr/local/bin/entrypoint
ENTRYPOINT ["entrypoint"]

WORKDIR $CLONEDIR
