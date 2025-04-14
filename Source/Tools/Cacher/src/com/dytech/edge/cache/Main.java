/*
 * Created on Feb 16, 2005
 */
package com.dytech.edge.cache;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main {
  private Main() {
    throw new Error("Do not invoke");
  }

  public static void main(String[] args) throws Exception {
    setCookieHandler();

    Service daemon = new Service();
    daemon.start();
  }

  private static void setCookieHandler() {
    CookieHandler.setDefault(
        new CookieHandler() {
          private Map<String, List<String>> cookies = new HashMap<String, List<String>>();

          @Override
          public Map<String, List<String>> get(URI uri, Map<String, List<String>> arg1)
              throws IOException {
            HashMap<String, List<String>> reqCookies = new HashMap<String, List<String>>();
            reqCookies.put("Cookie", cookies.get(uri.getHost())); // $NON-NLS-1$
            return reqCookies;
          }

          @Override
          public void put(URI uri, Map<String, List<String>> headers) throws IOException {
            List<String> respCookies = headers.get("Set-Cookie"); // $NON-NLS-1$
            if (respCookies != null) {
              List<String> perHost = cookies.get(uri.getHost());
              if (perHost == null) {
                perHost = new ArrayList<String>();
                cookies.put(uri.getHost(), perHost);
              }
              for (String cookie : respCookies) {
                int ind = cookie.indexOf(';');
                cookie = cookie.substring(0, ind);
                perHost.add(cookie);
              }
            }
          }
        });
  }
}
