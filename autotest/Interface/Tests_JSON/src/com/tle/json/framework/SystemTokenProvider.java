package com.tle.json.framework;

public class SystemTokenProvider implements TokenProvider {
  private final String password;

  public SystemTokenProvider(String password) {
    this.password = password;
  }

  @Override
  public String getToken() {
    return "system_token=" + password;
  }
}
