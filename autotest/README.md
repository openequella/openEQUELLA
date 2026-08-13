# openEQUELLA end-to-end tests

The browser-driven test suite: Selenium tests written against a real, running openEQUELLA, plus the
page-object library they are built on.

**Never run these against a system you care about.** They delete and re-create a fixed set of
institutions, including the standard `vanilla` institution.

## Layout

```
autotest/
├── build.sbt         The one project: the tests, and the tasks that install and drive a server
├── config/           Configuration - see below
├── docs/             Notes on particular styles of test
├── institutions/     Institution fixtures, imported by `sbt setupForTests`
├── IntegTester/      Support services the tests need, started as part of the run
├── src/test/java     The tests, the com.tle.webtests framework, and the page objects
├── src/test/scala    Newer tests; this is where new ones should go
└── suites/           TestNG suite definitions
```

## Prerequisites

- **A browser driver on your `PATH`** — [chromedriver] or [geckodriver], matching an installed
  Chrome or Firefox.
- **Node** — the IntegTester front end is built with `npm ci` as part of the sbt build. Use the
  version in the repository's `.nvmrc`.
- **PostgreSQL**, if you want these tests to install openEQUELLA for you.
- **ImageMagick, FFmpeg and ExifTool**, for the tests that exercise file handling.

[chromedriver]: https://developer.chrome.com/docs/chromedriver/downloads
[geckodriver]: https://github.com/mozilla/geckodriver/releases

## Configuration

Copy the example and edit your copy — it is gitignored, so it stays on your machine:

```bash
cp config/resources/application.conf.example config/resources/application.conf
```

At a minimum, point it at your server:

```conf
server.url = "http://localhost:8080/"
server.password = systempassword
```

Anything you leave out falls back to `config/defaults.conf`. If you are using Chrome, also set
`webdriver.chrome.driver` to your `chromedriver` binary.

To run with a different configuration entirely — `config/docker-build.conf`, say — set
`AUTOTEST_CONFIG` to its path rather than editing `application.conf`:

```bash
AUTOTEST_CONFIG=autotest/config/docker-build.conf ./sbt "project autotest" test
```

## Choosing a target instance

The tests need an openEQUELLA to drive. That can be your dev install, an existing install, or one
these tests install for you.

### Against an install you already have

Add this to `JAVA_OPTS` in `manager/equellaserver-config.sh`:

```
-Dequella.autotest=true
```

For coverage reports, add the JaCoCo agent as well. `sbt "project autotest" show coverageJar` prints
the path to use:

```
-javaagent:{jacocojarpath}=output=tcpserver
```

Check that `learningedge-config/optional-config.properties` has correct `exiftool.path` and
`ffmpeg.path` values, or the tests that use them will fail.

### Installing from an installer zip

Point `application.conf` at the zip:

```conf
install {
  zip = ${HOME}"/equella/Equella/Installer/target/equella-installer-2026.1.zip"
}
```

Then:

```bash
./sbt "project autotest" installEquella
```

By default that installs into `equella-install/`, configures an admin URL of `http://localhost:8080`,
turns on the autotest and coverage options, and expects a PostgreSQL database `equellatests` at
`localhost:5432` with user `equellatests` and password `password`. To create it:

```bash
sudo -u postgres psql
 CREATE DATABASE equellatests;
 CREATE USER equellatests WITH PASSWORD 'password';
 GRANT ALL PRIVILEGES ON equellatests TO equellatests;
```

Start and stop it with the `startEquella` and `stopEquella` tasks, or the scripts in
`equella-install/manager`.

## Setting up for tests

Set the admin password and initialise the default schema:

```bash
./sbt "project autotest" configureInstall
```

Skip this if you have already done it by hand — just make sure the server admin password matches
`server.password`. Running it against an already-configured install errors out.

Then import the institution fixtures, which the tests expect to exist:

```bash
./sbt "project autotest" setupForTests
```

## Running the tests

There are two kinds of test here, split into one sbt configuration per framework:

```bash
./sbt "project autotest" test            # the TestNG suites
./sbt "project autotest" ScalaTest/test  # the ScalaTest suites
```

They are kept apart because the TestNG suites get sharded across parallel CI jobs, and anything
sharing the `Test` configuration would be re-run by every shard. The split is by framework rather
than by package, so a new ScalaTest suite is picked up wherever it lives.

`testOnly` has to name the configuration too:

```bash
./sbt "project autotest" "ScalaTest/testOnly equellatests.tests.SanityTest"
```

Note that `testOnly` does **not** work for the TestNG side: sbt-testng-plugin ignores the filter and
runs whatever `tests.suitenames` lists. To run a subset, point `suitenames` at a suite file that
names only what you want.

Which TestNG suites run is configuration, not a command-line argument:

```conf
tests {
  suitenames = ["all.yaml"]
  parallel = false
}
```

`suites/all.yaml` is the full set. `suites/testng.xml` groups the older per-area XML suites and is
the default. The `suites/testng-remote-*.xml` suites drive LMS integrations — Blackboard, Canvas,
Moodle — and need those external servers, so they are run by hand rather than as part of any
automated run.

Expect a full run to take 30-45 minutes and to open browser windows you should not touch. To avoid
that, run headless:

```conf
webdriver {
  chrome {
    driver = ${HOME}"/bin/chromedriver"
    headless = true
  }
}
```

The ScalaTest suites are annotated `@NewUIOnly`, so — exactly as for the TestNG tests carrying that
annotation — they are skipped unless the `OLD_TEST_NEWUI` environment variable is `true`.

## Where the results go

| What | Where |
|---|---|
| TestNG HTML report | `target/testng/index.html` |
| TestNG JUnit XML | `target/testng/junitreports/` |
| ScalaTest JUnit XML | `target/scalatest-junitreports/` |
| Screenshots and failure artefacts | `target/test-reports/` |
| Server logs (if these tests installed it) | `equella-install/logs/` |

## Coverage report

If the instance was set up with the JaCoCo agent, you can produce an HTML coverage report.

Optionally build a source zip first, so the report can link through to the source. From the
repository root:

```bash
./sbt writeSourceZip
```

It prints the path; put that in your `application.conf`:

```conf
install.sourcezip = ${HOME}"/equella/Equella/Source/Server/equellaserver/target/equella-sources.zip"
```

Then:

```bash
./sbt "project autotest" coverageReport
```

The report lands in `target/coverage-report/index.html`.

## Writing tests

- New tests go in `src/test/scala`, under ScalaTest.
- Use the page-object pattern — see the existing objects under
  `src/test/java/com/tle/webtests/pageobject/`.
- Prefer semantic and ARIA locators, then `data-testid`, then text. Avoid XPath.
- Wait explicitly for what you need; never sleep.
- For property-based tests over browser state, see [docs/statefultesting.md](docs/statefultesting.md).
