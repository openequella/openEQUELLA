package com.tle.json.framework;

public class TimeoutException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public TimeoutException() {
    super();
  }

  public TimeoutException(String message) {
    super(message);
  }
}
