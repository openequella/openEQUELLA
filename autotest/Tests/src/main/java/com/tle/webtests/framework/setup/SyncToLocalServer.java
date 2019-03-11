package com.tle.webtests.framework.setup;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.WebDriverPool;
import com.tle.webtests.framework.setup.InstitutionModel.InstitutionData;
import com.tle.webtests.pageobject.UndeterminedPage;
import com.tle.webtests.pageobject.institution.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

public class SyncToLocalServer {
  private final TestConfig testConfig;

  public SyncToLocalServer() {
    testConfig = new TestConfig(getClass(), true);
  }

  public void importInstitution(InstitutionData inst) {
    FirefoxDriver driver = getDriver();
    try {
      PageContext context = new PageContext(driver, testConfig, testConfig.getAdminUrl());
      String instutionUrl = testConfig.getInstitutionUrl(inst.getShortName(), inst.isHttps());
      InstitutionListTab listTab =
          new ServerAdminLogonPage(context)
              .get()
              .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
      ImportTab importTab = new ImportTab(context);
      UndeterminedPage<InstitutionTabInterface> choice =
          new UndeterminedPage<InstitutionTabInterface>(context, listTab, importTab);
      InstitutionTabInterface currentTab = choice.load();
      if (currentTab == listTab) {
        if (listTab.institutionExists(instutionUrl)) {
          StatusPage<InstitutionTabInterface> statusPage = listTab.delete(instutionUrl, choice);
          Assert.assertTrue(statusPage.waitForFinish());
          currentTab = statusPage.back();
        }
        if (currentTab != importTab) {
          importTab = listTab.importTab();
        }
      }
      Assert.assertTrue(
          importTab
              .importInstitution(
                  instutionUrl, inst.getShortName(), inst.getInstitutionFile().toPath())
              .waitForFinish());
    } finally {
      driver.quit();
    }
  }

  @SuppressWarnings("nls")
  public void exportInstitution(InstitutionData inst) {
    FirefoxDriver driver = getDriver();
    try {
      PageContext context = new PageContext(driver, testConfig, testConfig.getAdminUrl());
      InstitutionListTab listTab =
          new ServerAdminLogonPage(context)
              .load()
              .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
      String instutionUrl = testConfig.getInstitutionUrl(inst.getShortName(), inst.isHttps());
      ExportPage exportPage = listTab.export(instutionUrl);
      exportPage.removeAuditLogs();
      StatusPage<InstitutionListTab> statusPage = exportPage.export();
      Assert.assertTrue(statusPage.waitForFinish(), "");

      Cookie cookie = driver.manage().getCookieNamed("JSESSIONID");
      String link = statusPage.getDownloadLink();
      try {
        if (instutionUrl.startsWith("https")) {
          // Create a trust manager that does not validate certificate
          // chains
          TrustManager[] trustAllCerts =
              new TrustManager[] {
                new X509TrustManager() {
                  @Override
                  public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                  }

                  @Override
                  public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                      throws CertificateException {
                    // nothing
                  }

                  @Override
                  public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                      throws CertificateException {
                    // nothing
                  }
                }
              };

          // Install the all-trusting trust manager
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, new java.security.SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

          // Create all-trusting host name verifier
          HostnameVerifier allHostsValid =
              new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                  return true;
                }
              };

          // Install the all-trusting host verifier
          HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }

        HttpURLConnection con = (HttpURLConnection) new URL(link).openConnection();
        con.addRequestProperty("Cookie", "JSESSIONID=" + cookie.getValue());
        FileOutputStream out = new FileOutputStream(inst.getInstitutionFile());
        InputStream stream = con.getInputStream();
        byte[] buf = new byte[32768];
        int len;
        while ((len = stream.read(buf)) != -1) {
          out.write(buf, 0, len);
        }
        stream.close();
        out.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    } finally {
      driver.quit();
    }
  }

  public static class FixedDriver implements WebDriverPool {
    private FirefoxDriver driver;

    public FixedDriver(FirefoxDriver driver) {
      this.driver = driver;
    }

    @Override
    public WebDriver getDriver() {
      return driver;
    }

    @Override
    public void releaseDriver(WebDriver driver) {
      // never
    }
  }

  private FirefoxDriver getDriver() {
    FirefoxBinary binary;
    if (testConfig.getFirefoxBinary() != null) {
      binary = new FirefoxBinary(new File(testConfig.getFirefoxBinary()));
    } else {
      binary = new FirefoxBinary();
    }
    binary.setTimeout(SECONDS.toMillis(120));
    FirefoxProfile profile = new FirefoxProfile();
    // profile.addExtension(getClass(), "firebug-1.7.3-fx.xpi");
    profile.setPreference("extensions.firebug.currentVersion", "999");
    profile.setPreference("dom.max_script_run_time", 120);
    profile.setPreference("dom.max_chrome_script_run_time", 120);

    FirefoxOptions options = new FirefoxOptions();
    options.setBinary(binary);
    options.setProfile(profile);
    return new FirefoxDriver(options);
  }
}
