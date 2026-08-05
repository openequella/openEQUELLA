# Building and Testing openEQUELLA in Docker

This directory contains a `Dockerfile` and helper scripts for building openEQUELLA — and running its
end-to-end test suite — inside a Docker container.

## Full build

The `Dockerfile` in this directory produces an image that can be used for completely fresh builds
and also the execution of the end-to-end tests. This could also be used for an image to run a CI
type build, and so in a similar thought can also be used to diagnose CI build issues.

It uses eclipse-temurin:21 as its base image. For the installation of dependent build tools,
SBT relies on the `./sbt` wrapper in the repository to ensure the correct version is always used.
NVM is installed directly in the Dockerfile via the official installation script, and the repository's
`.nvmrc` is then used to set up the correct Node/NPM version. Both are therefore always in sync
with what is currently in the repository.

As part of the build it clones the openEQUELLA repository and uses the `.nvmrc` to install an
initial version of Node/NVM. However, SBT and its dependencies will only be installed at the first
execution of `./sbt` — this minimises the image size.

The image has a Postgres server all setup with a database and user matching those in the default
autotest configuration. This assists with being able to run openEQUELLA in the container and then
the end-to-end selenium tests. To support this, Google Chrome and the matching ChromeDriver are
installed in the build image via two helper scripts in this directory:

- **`install-chrome`** — Downloads and installs Google Chrome from the official `.deb` package.
  By default it installs the version pinned in the script via `CUSTOMIZE_CHROME_VERSION` (currently
  `143.0.7499.192`). To use the latest stable release instead, simply clear that variable in the
  script before building.

- **`install-chromedriver`** — Reads the version of the Chrome binary already installed
  (`google-chrome --version`), then queries the
  [Chrome for Testing JSON endpoint](https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json)
  to find and download the exactly matching ChromeDriver for Linux 64-bit. The driver is placed at
  the path passed as the first argument to the script (the build has it land at
  `/usr/local/bin/chromedriver`). If you wish to run the tests, make sure your configuration points to this ChromeDriver path.

- **`install-imagemagick`** — Downloads the pinned **ImageMagick 7.1.2-18** AppImage from the
  official GitHub releases page, extracts it, and installs it to `/opt/imagemagick/magick`. This
  ensures the build environment uses the `magick`-first CLI syntax required by openEQUELLA (see the
  contributing guide for details on why ImageMagick 6.x from `apt` must not be used).

### Building the image

Building is very simple. Assuming you'd like to call the image `oeq-full-build`, the standard
command to build from the official public repository is:

```bash
docker build -t oeq-full-build .
```

**Using Custom or Private Repositories**

By default, the Dockerfile clones the official openEQUELLA GitHub repository. If you are developing
on a fork or a private internal repository, you can override the source location using the
`REPO_URL` build argument.

To build from a **public fork**:

```bash
docker build -t oeq-full-build \
  --build-arg REPO_URL="https://github.com/your-username/openEQUELLA.git" .
```

To build from a **private repository**, you must inject a Personal Access Token (PAT) directly into
the URL so the Docker builder can authenticate during the clone step:

```bash
docker build -t oeq-full-build \
  --build-arg REPO_URL="https://<username>:<token>@gitlab.com/example/openequella.git" .
```

### Running the image

To then run the image from the previous build step, you:

    docker run -it --rm oeq-full-build

Take note of the command line options there:

- `-it` provides for an interactive terminal (required to use the provided shell)
- `--rm` completely removes the container when you exit (this is optional, and depends what you're
  doing)

### Building openEQUELLA

When you connect to the running instance you should find yourself in a directory which is a fresh
`git clone` of the repository. So here you can now execute all the usual commands. To get yourself
an installerZip — which can then be used to start an openEQUELLA and run the tests similar to how
the CI builds do, you'd execute:

    ./sbt installerZip

The first time around this can take a while, as it has no cache for your maven/ivy artefacts and so
you'll see it needs to download _everything_ (including SBT and its dependencies). But just like
your local machine, after that's done it will have them for future runs. (This is something to
consider when using the `--rm` option when running the image.)

### Running tests

Mainly you would refer to how the CI scripts run the tests, but essentially you need to take the
steps:

1. Make sure you have environment variables setup — especially pointing to the configuration file
2. Use the `autotest` tasks to setup a local openEQUELLA with the installer ZIP you built
3. Run the tests

To set up the minimum of environment variables you'd consider doing:

```bash
export AUTOTEST_CONFIG=autotest/codebuild.conf  # set the configuration file to control things
export EQ_EXIFTOOL_PATH=/usr/bin/exiftool
export OLD_TEST_NEWUI=true                      # true if you want the tests in New UI mode

# Override the ChromeDriver path to match where install-chromedriver placed it in the image.
# codebuild.conf defaults to ${HOME}/chromedriver which won't exist here.
export SBT_OPTS="-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver"
```

Next to utilise the `autotest` project tasks to establish an environment:

    ./sbt "project autotest" installEquella startEquella

Treat server startup as asynchronous: only continue with configuration or test execution once
openEQUELLA is actually reachable on the expected URL.

    ./sbt "project autotest" configureInstall setupForTests

Assuming that all passed, then you can run tests with:

    ./sbt "project autotest" OldTests/test

