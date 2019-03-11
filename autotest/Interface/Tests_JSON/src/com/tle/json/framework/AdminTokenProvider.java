package com.tle.json.framework;

public class AdminTokenProvider implements TokenProvider {
  private final String password;

  public AdminTokenProvider(String password) {
    this.password = password;
  }

  @Override
  public String getToken() {
    return "admin_token=" + password;
  }
}
