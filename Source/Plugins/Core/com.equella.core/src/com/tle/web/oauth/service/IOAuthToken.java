package com.tle.web.oauth.service;

import java.time.Instant;

public interface IOAuthToken {

  String getToken();

  Instant getExpiry();
}
