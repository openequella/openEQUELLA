package com.tle.webtests.test.files;

import java.net.URL;

public final class Attachments {

  private Attachments() {
    throw new UnsupportedOperationException();
  }

  public static URL get(String file) {
    return Attachments.class.getResource(file);
  }
}
