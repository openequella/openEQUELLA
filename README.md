# EQUELLA Selenium Tests

Install SBT and one of the drivers for Chrome or Firefox -
[chromedriver](https://sites.google.com/a/chromium.org/chromedriver/) or [geckodriver](https://github.com/mozilla/geckodriver/releases).

Copy the `Tests/config/application.conf.example` to `Tests/config/application.conf` and
configure the `server.url` to point to your local EQUELLA, for example:

```conf
server.url = "http://localhost:8080/"
```

For Chrome you must also edit `webdriver.chrome.driver` to point to the `chromedriver` binary.

## Running all tests

```bash
sbt test
```

You can view the HTML report at `Tests/target/testng/index.html`

