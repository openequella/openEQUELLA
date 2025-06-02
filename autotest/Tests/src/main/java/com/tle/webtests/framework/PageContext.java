package com.tle.webtests.framework;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PageContext {
  private WebDriver driver;
  private final String baseUrl;
  private String integUrl;
  private String namePrefix;
  private String subPrefix;
  private String prefix = "";
  private final Map<Object, Object> attr = Maps.newHashMap();
  private final TestConfig testConfig;

  public PageContext(WebDriver driver, TestConfig testConfig, String baseUrl) {
    Preconditions.checkNotNull(baseUrl);
    this.testConfig = testConfig;
    this.baseUrl = baseUrl;
    this.driver = driver;
  }

  public PageContext(PageContext existing, String baseUrl) {
    this.driver = existing.driver;
    this.testConfig = existing.testConfig;
    attr.putAll(existing.attr);
    this.integUrl = existing.integUrl;
    this.namePrefix = existing.namePrefix;
    this.subPrefix = existing.subPrefix;
    this.prefix = existing.prefix;
    this.baseUrl = baseUrl;
  }

  public WebDriver getDriver() {
    return driver;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
    changePrefix();
  }

  private void changePrefix() {
    if (namePrefix != null) {
      prefix = namePrefix + " - ";
      if (subPrefix != null) prefix += subPrefix + " ";
    } else {
      prefix = "";
    }
  }

  public String getFullName(String name) {
    return prefix + name;
  }

  public String getSubPrefix() {
    return subPrefix;
  }

  public void setSubPrefix(String subPrefix) {
    this.subPrefix = subPrefix;
    changePrefix();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getIntegUrl() {
    return integUrl;
  }

  public void setIntegUrl(String integUrl) {
    this.integUrl = integUrl;
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(Object key) {
    return (T) attr.get(key);
  }

  public void setAttribute(Object key, Object value) {
    attr.put(key, value);
  }

  public TestConfig getTestConfig() {
    return testConfig;
  }

  public WebDriver getCurrentDriver() {
    return driver;
  }

  /**
   * Creates a new {@link WebDriverWait} instance using the current driver and the standard timeout
   * defined in the test configuration.
   *
   * @return a new {@link WebDriverWait} instance
   */
  public WebDriverWait createWaiter() {
    return new WebDriverWait(driver, testConfig.getStandardTimeout());
  }
}
