package com.tle.resttests.util;

public class OAuthClient {
  private String secret;
  private String clientId;
  private String name;
  private String url;
  private String userId;
  private String username;
  private String uuid;
  private boolean defaultRedirect;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isDefaultRedirect() {
    return defaultRedirect;
  }

  public void setDefaultRedirect(boolean defaultRedirect) {
    this.defaultRedirect = defaultRedirect;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
