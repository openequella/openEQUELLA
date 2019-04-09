package com.tle.web.oauth.service;

public interface IOAuthClient {

  String getUserId();

  String getClientId();

  String getRedirectUrl();

  String getClientSecret();

  boolean secretMatches(String clientSecret);
}
