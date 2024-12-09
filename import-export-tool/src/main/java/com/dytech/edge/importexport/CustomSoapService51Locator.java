package com.dytech.edge.importexport;

import com.tle.web.remoting.soap.SoapService51ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.axis.client.Service;
import org.apache.axis.transport.http.HTTPTransport;

public class CustomSoapService51Locator extends SoapService51ServiceLocator {
  public CustomSoapService51Locator() {
    setMaintainSession(true);
  }

  @SuppressWarnings("unchecked")
  public List<String> getCookiesForUrl(URL url) {
    // Really, REALLY dodgical hax
    Method transportGetter;
    try {
      transportGetter =
          Service.class.getDeclaredMethod("getTransportForURL", URL.class); // $NON-NLS-1$

      transportGetter.setAccessible(true);
      final HTTPTransport t = (HTTPTransport) transportGetter.invoke(this, url);
      final Field f = HTTPTransport.class.getDeclaredField("cookie"); // $NON-NLS-1$
      f.setAccessible(true);
      Object cooks = f.get(t);
      if (cooks == null) {
        return Collections.EMPTY_LIST;
      } else if (cooks instanceof String) {
        return Collections.singletonList((String) cooks);
      } else {
        return Arrays.asList((String[]) cooks);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
