package com.tle.webtests.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.tle.webtests.pageobject.DownloadFilePage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.jul.Log4jBridgeHandler;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.log.GenericLogEntry;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.bidi.log.StackFrame;
import org.openqa.selenium.bidi.log.StackTrace;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class StandardDriverFactory {

  static {
    // Selenium logs through java.util.logging, which log4j2.yaml does not govern, so its output
    // arrives unformatted and unfiltered. Bridge it into log4j2 so every line lands in the one
    // place in the one format. Removing the root handlers stops each record printing twice, and
    // propagating levels lets a suppressed record be dropped before its LogRecord is built.
    // Selenium logs nothing before a driver exists and every driver is built here, so installing
    // on class initialisation catches all of it.
    Log4jBridgeHandler.install(true, null, true);
  }

  Logger logger = LoggerFactory.getLogger(StandardDriverFactory.class);
  private final String firefoxBinary;
  private final String chromeBinary;
  private final boolean chrome;
  private final String gridUrl;
  private final boolean firefoxHeadless;
  private final boolean chromeHeadless;
  private Proxy proxy;
  private static ChromeDriverService _chromeService;

  public StandardDriverFactory(TestConfig config) {
    this.chrome = config.isChromeDriverSet();
    this.chromeBinary = config.getChromeBinary();
    this.firefoxBinary = config.getFirefoxBinary();
    this.gridUrl = config.getGridUrl();
    this.firefoxHeadless = config.getBooleanProperty("webdriver.firefox.headless", false);
    this.chromeHeadless = config.getBooleanProperty("webdriver.chrome.headless", false);
    String proxyHost = config.getProperty("proxy.host");
    if (proxyHost != null) {
      int port = Integer.parseInt(config.getProperty("proxy.port"));
      proxy = new Proxy();
      String proxyHostPort = proxyHost + ":" + port;
      proxy.setHttpProxy(proxyHostPort);
      proxy.setSslProxy(proxyHostPort);
    }
  }

  private static synchronized ChromeDriverService getChromeService() throws IOException {
    if (_chromeService == null) {
      _chromeService = ChromeDriverService.createDefaultService();
      _chromeService.start();
    }
    return _chromeService;
  }

  private void setFirefoxPreferences(FirefoxProfile profile, String downDir) {
    profile.setPreference("dom.max_script_run_time", 120);
    profile.setPreference("dom.max_chrome_script_run_time", 120);
    profile.setPreference("browser.download.useDownloadDir", true);
    profile.setPreference("browser.download.folderList", 2);
    profile.setPreference("browser.download.dir", downDir);
    profile.setPreference("extensions.firebug.currentVersion", "999");
    profile.setPreference(
        "browser.helperApps.neverAsk.saveToDisk", "application/zip,image/png,text/xml");
    profile.setPreference("security.mixed_content.block_active_content", false);
    profile.setPreference("security.mixed_content.block_display_content", false);
  }

  public WebDriver getDriver(Class<?> clazz) throws IOException {
    String downDir = DownloadFilePage.getDownDir().getAbsolutePath();
    WebDriver driver = chrome ? setupChrome(downDir) : setupFirefox(downDir);
    setupLogInspector(driver);

    return driver;
  }

  private void enableHeadlessDownloads(RemoteWebDriver cdriver, String downDir) throws IOException {
    Map<String, Object> commandParams = new HashMap<>();
    commandParams.put("cmd", "Page.setDownloadBehavior");
    Map<String, String> params = new HashMap<>();
    params.put("behavior", "allow");
    params.put("downloadPath", downDir);
    commandParams.put("params", params);
    ObjectMapper objectMapper = new ObjectMapper();
    HttpClient httpClient = HttpClientBuilder.create().build();
    String command = objectMapper.writeValueAsString(commandParams);
    String u =
        getChromeService().getUrl().toString()
            + "/session/"
            + cdriver.getSessionId()
            + "/chromium/send_command";
    HttpPost request = new HttpPost(u);
    request.addHeader("content-type", "application/json");
    request.setEntity(new StringEntity(command));
    try {
      httpClient.execute(request).getEntity().getContent().close();
    } catch (IOException e2) {
      e2.printStackTrace();
    }
  }

  private WebDriver setupChrome(String downDir) throws IOException {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    ChromeOptions options = new ChromeOptions();
    if (chromeBinary != null) {
      options.setBinary(chromeBinary);
    }
    options.enableBiDi();
    // Enabling BIDI results in a known issue where alerts are not properly shown. Currently, a
    // workaround
    // is to set "unhandledPromptBehavior" to "ignore.
    // See https://github.com/SeleniumHQ/selenium/issues/14468 for details.
    options.setCapability(
        CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
    options.addArguments("test-type");
    options.addArguments("disable-gpu");
    options.addArguments("no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    if (chromeHeadless) {
      options.addArguments("--headless=new");
      options.addArguments("--lang=en-US");
    }
    options.addArguments("window-size=1200,800");

    Map<String, Object> prefs = Maps.newHashMap();
    prefs.put("intl.accept_languages", "en-US");
    prefs.put("download.default_directory", downDir);
    prefs.put("profile.password_manager_enabled", false);

    options.setExperimentalOption("prefs", prefs);
    if (proxy != null) {
      capabilities.setCapability(CapabilityType.PROXY, proxy);
    }
    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
    // Use a driver that normalises Chrome's "-32000 Node with given id does not belong to the
    // document" inspector error into a standard StaleElementReferenceException (see
    // StaleNodeTranslatingRemoteWebDriver and Selenium issue #15401).
    RemoteWebDriver rdriver =
        new StaleNodeTranslatingRemoteWebDriver(getChromeService().getUrl(), capabilities);
    if (chromeHeadless) {
      enableHeadlessDownloads(rdriver, downDir);
    }
    return new Augmenter().augment(rdriver);
  }

  private WebDriver setupFirefox(String downDir) {
    FirefoxProfile profile = new FirefoxProfile();
    profile.addExtension(getClass(), "firebug-1.10.2-fx.xpi");
    profile.addExtension(getClass(), "firepath-0.9.7-fx.xpi");
    setFirefoxPreferences(profile, downDir);
    FirefoxOptions options = new FirefoxOptions();
    if (firefoxBinary != null) {
      options.setBinary(new File(firefoxBinary).getAbsolutePath());
    }
    options.enableBiDi();
    if (firefoxHeadless) {
      options.addArguments("-headless");
    }
    options.setCapability("moz:webdriverClick", false);
    if (proxy != null) {
      options.setCapability(CapabilityType.PROXY, proxy);
    }
    options.setProfile(profile);
    WebDriver driver = new FirefoxDriver(options);
    driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(5));

    return driver;
  }

  // Set up BIDI log inspector to capture unexpected JavaScript exceptions thrown from the
  // interaction with OEQ.
  private void setupLogInspector(WebDriver driver) {
    try (LogInspector logInspector = new LogInspector(driver)) {
      logInspector.onJavaScriptLog(this::captureLogs);
      logInspector.onConsoleEntry(this::captureLogs);
    } catch (IllegalArgumentException e) {
      logger.error("Failed to set up BIDI log inspector", e);
    }
  }

  private String buildStackFrameMessage(StackFrame frame) {
    return String.format(
        "\n\s\s\sat %s (%s:%d:%d)",
        frame.getFunctionName(), frame.getUrl(), frame.getLineNumber(), frame.getColumnNumber());
  }

  private String getFullJavascriptLog(GenericLogEntry logEntry) {
    Optional<Stream<StackFrame>> stackTraceFrames =
        Optional.ofNullable(logEntry.getStackTrace())
            .map(StackTrace::getCallFrames)
            .map(Collection::stream);

    Optional<String> stackTraceString =
        stackTraceFrames.map(
            frame ->
                frame
                    .map(this::buildStackFrameMessage)
                    .collect(Collectors.joining("", "\nStack Trace:", "")));

    return stackTraceString.map(st -> logEntry.getText() + st).orElseGet(logEntry::getText);
  }

  private void captureLogs(GenericLogEntry logEntry) {
    Marker jsMarker = MarkerFactory.getMarker("JS_ERROR");
    switch (logEntry.getLevel()) {
      case LogLevel.ERROR:
        String error = getFullJavascriptLog(logEntry);
        logger.error(jsMarker, error);
        break;
      case LogLevel.WARNING:
        String warning = getFullJavascriptLog(logEntry);
        logger.warn(jsMarker, warning);
        break;
      case LogLevel.INFO:
        logger.info(jsMarker, logEntry.getText());
        break;
      case LogLevel.DEBUG:
        logger.debug(jsMarker, logEntry.getText());
        break;
      default:
        // No need to capture other logs.
    }
  }
}
