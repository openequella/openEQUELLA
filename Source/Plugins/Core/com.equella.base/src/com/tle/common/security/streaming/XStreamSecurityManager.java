package com.tle.common.security.streaming;

import com.thoughtworks.xstream.XStream;

public class XStreamSecurityManager {
  public static void applyPolicy(XStream xstream) {
    XStream.setupDefaultSecurity(xstream);
    // Anything you want to be XStream'd needs to be allowed here
    xstream.allowTypesByWildcard(
        new String[] {
          "com.tle.**", "com.dytech.**",
        });
  }

  // Helper method to have ALL XStream instances be covered by
  // the oEQ policy(ies) in this manager.
  public static XStream newXStream() {
    XStream xs = new XStream();
    applyPolicy(xs);
    return xs;
  }
}
