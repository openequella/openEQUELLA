package com.tle.webtests.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.DownloadFilePage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

public class StandardDriverFactory {

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
    WebDriver driver;
    String downDir = DownloadFilePage.getDownDir().getAbsolutePath();
    if (!Check.isEmpty(gridUrl) && !clazz.isAnnotationPresent(LocalWebDriver.class)) {
      FirefoxProfile profile = new FirefoxProfile();
      setFirefoxPreferences(profile, downDir);
      profile.addExtension(
          new File(
              AbstractPage.getPathFromUrl(
                  StandardDriverPool.class.getResource("firebug-1.10.2-fx.xpi"))));
      profile.addExtension(
          new File(
              AbstractPage.getPathFromUrl(
                  StandardDriverPool.class.getResource("firepath-0.9.7-fx.xpi"))));
      FirefoxOptions options = new FirefoxOptions().setProfile(profile);
      if (firefoxHeadless) {
        options.addArguments("-headless");
      }
      driver = new RemoteWebDriver(new URL(gridUrl), options);
      RemoteWebDriver rd = (RemoteWebDriver) driver;
      rd.setFileDetector(new LocalFileDetector());
      driver = new Augmenter().augment(driver);
    } else {
      if (chrome) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        ChromeOptions options = new ChromeOptions();
        if (chromeBinary != null) {
          options.setBinary(chromeBinary);
        }
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
        RemoteWebDriver rdriver = new RemoteWebDriver(getChromeService().getUrl(), capabilities);
        driver = new Augmenter().augment(rdriver);
        if (chromeHeadless) {
          enableHeadlessDownloads(rdriver, downDir);
        }
      } else {
        FirefoxBinary binary;
        if (firefoxBinary != null) {
          binary = new FirefoxBinary(new File(firefoxBinary));
        } else {
          binary = new FirefoxBinary();
        }
        FirefoxProfile profile = new FirefoxProfile();
        profile.addExtension(getClass(), "firebug-1.10.2-fx.xpi");
        profile.addExtension(getClass(), "firepath-0.9.7-fx.xpi");
        setFirefoxPreferences(profile, downDir);
        FirefoxOptions options = new FirefoxOptions();
        if (firefoxHeadless) {
          options.addArguments("-headless");
        }
        options.setCapability("moz:webdriverClick", false);
        if (proxy != null) {
          options.setCapability(CapabilityType.PROXY, proxy);
        }
        options.setBinary(binary);
        options.setProfile(profile);
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.MINUTES);
      }
    }
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
}
