# A bare image ready for building oEQ and also running test suites

FROM eclipse-temurin:21 AS basebuild

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN \
  apt-get update \
  && apt-get install -y \
    build-essential \
    vim \
    less \
    iproute2 \
# libtinfo6 is needed for Purescript tools
    libtinfo6 \
    ffmpeg \
    git \
    libimage-exiftool-perl \
    unzip \
    wget \
    jq \
# tidy up APT and save some space
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ARG DIR_BIN=/usr/local/bin

COPY install-imagemagick $DIR_BIN
RUN install-imagemagick

FROM basebuild AS basebuild-testtools

# Install Google Chrome and chromedriver
COPY install-chrome $DIR_BIN
COPY install-chromedriver $DIR_BIN

RUN \
  apt-get update \
  && install-chrome \
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

FROM basebuild-testtools AS buildenv

ARG CLONEDIR=openequella

# The target repository to clone.
# NOTE: If overriding with a private repository (e.g., internal GitLab), you MUST
# inject a Personal Access Token into the URL during the build command.
# Example: --build-arg REPO_URL="https://<username>:<token>@gitlab.com/..."
ARG REPO_URL="https://github.com/openequella/openEQUELLA.git"

RUN \
# Use NVM for managing node versions
  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash \
  && export NVM_DIR="$HOME/.nvm" \
  && . "$NVM_DIR/nvm.sh" \
# An initial clone also provides access to the .nvmrc to setup node
  && git clone ${REPO_URL} $CLONEDIR \
  && cd $CLONEDIR \
# Establish initial version of Node/NPM, may still want to update later when using image
  && nvm install

COPY entrypoint-buildenv /usr/local/bin/entrypoint
ENTRYPOINT ["entrypoint"]

WORKDIR $CLONEDIR
