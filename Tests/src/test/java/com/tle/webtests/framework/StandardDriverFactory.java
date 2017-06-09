package com.tle.webtests.framework;

import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.webtests.pageobject.AbstractPage;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class StandardDriverFactory {

    private final String firefoxBinary;
    private final String chromeBinary;
    private final boolean chrome;
    private final String gridUrl;
    private final boolean headless;
    private Proxy proxy;
    private static ChromeDriverService _chromeService = ChromeDriverService.createDefaultService();

    public StandardDriverFactory(TestConfig config) {
        this.chrome = config.isChromeDriverSet();
        this.chromeBinary = config.getChromeBinary();
        this.firefoxBinary = config.getFirefoxBinary();
        this.gridUrl = config.getGridUrl();
        this.headless = Boolean.parseBoolean(config.getProperty("webdriver.chrome.headless", "false"));
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
        if (!_chromeService.isRunning())
        {
            _chromeService.start();
        }
        return _chromeService;
    }

    public WebDriver getDriver(Class<?> clazz) throws IOException {
        WebDriver driver;
        if (!Check.isEmpty(gridUrl) && !clazz.isAnnotationPresent(LocalWebDriver.class)) {
            DesiredCapabilities capability = DesiredCapabilities.firefox();
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("dom.max_script_run_time", 120);
            profile.setPreference("dom.max_chrome_script_run_time", 120);
            profile.setPreference("browser.download.useDownloadDir", true);
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.dir", FileUtils.getTempDirectory().getAbsolutePath());
            profile.setPreference("extensions.firebug.currentVersion", "999");
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip,image/png,text/xml");
            profile.setPreference("security.mixed_content.block_active_content", false);
            profile.setPreference("security.mixed_content.block_display_content", false);
            profile.addExtension(new File(AbstractPage.getPathFromUrl(StandardDriverPool.class
                    .getResource("firebug-1.10.2-fx.xpi"))));
            profile.addExtension(new File(AbstractPage.getPathFromUrl(StandardDriverPool.class
                    .getResource("firepath-0.9.7-fx.xpi"))));
            capability.setCapability(FirefoxDriver.PROFILE, profile);
            driver = new RemoteWebDriver(new URL(gridUrl), capability);
            RemoteWebDriver rd = (RemoteWebDriver) driver;
            rd.setFileDetector(new LocalFileDetector());
            driver = new Augmenter().augment(driver);
        } else {
            if (chrome) {
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                ChromeOptions options = new ChromeOptions();
                if (chromeBinary != null) {
                    options.setBinary(chromeBinary);
                }
                options.addArguments("test-type");
                options.addArguments("disable-gpu");
                options.addArguments("no-sandbox");
                if (headless) {
                    options.addArguments("headless");
                }
                options.addArguments("window-size=1200,800");

                Map<String, Object> prefs = Maps.newHashMap();
                prefs.put("intl.accept_languages", "en-US");
                prefs.put("download.default_directory", FileUtils.getTempDirectory().getAbsolutePath());
                prefs.put("profile.password_manager_enabled", false);

                options.setExperimentalOption("prefs", prefs);

                if (proxy != null) {
                    capabilities.setCapability(CapabilityType.PROXY, proxy);
                }
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                driver = new RemoteWebDriver(getChromeService().getUrl(), capabilities);
                driver = new Augmenter().augment(driver);
            } else {
                FirefoxBinary binary;
                if (firefoxBinary != null) {
                    binary = new FirefoxBinary(new File(firefoxBinary));
                } else {
                    binary = new FirefoxBinary();
                }
                binary.setTimeout(SECONDS.toMillis(120));
                FirefoxProfile profile = new FirefoxProfile();
                profile.addExtension(getClass(), "firebug-1.10.2-fx.xpi");
                profile.addExtension(getClass(), "firepath-0.9.7-fx.xpi");
                profile.setPreference("extensions.firebug.currentVersion", "999");
                profile.setPreference("dom.max_script_run_time", 120);
                profile.setPreference("dom.max_chrome_script_run_time", 120);
                profile.setPreference("browser.download.useDownloadDir", true);
                profile.setPreference("browser.download.folderList", 2);
                profile.setPreference("browser.download.dir", FileUtils.getTempDirectory().getAbsolutePath());
                profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                        "application/zip,image/png,text/xml");
                // profile.setEnableNativeEvents(true);
                profile.setPreference("security.mixed_content.block_active_content", false);
                profile.setPreference("security.mixed_content.block_display_content", false);
                DesiredCapabilities cap = DesiredCapabilities.firefox();
                if (proxy != null) {
                    cap.setCapability(CapabilityType.PROXY, proxy);
                }
                driver = new FirefoxDriver(binary, profile, cap);
                driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.MINUTES);
            }

        }
        return driver;
    }
}
