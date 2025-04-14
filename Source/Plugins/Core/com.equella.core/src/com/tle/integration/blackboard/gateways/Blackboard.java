/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.integration.blackboard.gateways;

import com.dytech.devlib.Md5;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.integration.blackboard.BlackBoardSessionData;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Blackboard {
  private static final int TIMEOUT = 60 * 1000;
  private static final Log LOGGER = LogFactory.getLog(Blackboard.class);
  protected static final int PASS_LENGTH = 32;

  protected URL url = null;

  public boolean isConfigured() {
    return url != null;
  }

  public void setURL(URL url) {
    LOGGER.info("Blackboard URL is " + url);
    this.url = url;
  }

  protected Collection<NameValue> convertParameters(NameValue[] parameters) {
    return Arrays.asList(parameters);
  }

  protected String getEncodedPassword(String pass) {
    Md5 digest = new Md5(pass);
    return digest.getStringDigest().toUpperCase();
  }

  protected String encode(String unenc) {
    try {
      unenc = URLEncoder.encode(unenc, Constants.UTF8);
    } catch (Exception e) {
      // Never happen
    }
    return unenc;
  }

  public PropBagEx invoke(BlackBoardSessionData data, String name, Collection<NameValue> parameters)
      throws IOException {
    return invoke(data, "Facade", name, parameters); // $NON-NLS-1$
  }

  public PropBagEx invoke(
      BlackBoardSessionData data, String servlet, String name, Collection<NameValue> parameters)
      throws IOException {
    long startTime = System.currentTimeMillis();

    BlindSSLSocketFactory.register();

    // Setup URLConnection to appropriate servlet/jsp
    URLConnection con = new URL(this.url, servlet).openConnection();

    con.setConnectTimeout(TIMEOUT);
    con.setReadTimeout(TIMEOUT);

    con.setDoInput(true);
    con.setDoOutput(true);

    String token = data.getBlackBoardSession();
    // BB7 contains '@@'
    final int expectedTokenLength = PASS_LENGTH + (token.startsWith("@@") ? 2 : 0);
    if (token.length() == expectedTokenLength) {
      con.setRequestProperty("Cookie", "session_id=" /* @@" */ + token + ";");
    }

    // Open output stream and send username and password
    PrintWriter conout = new PrintWriter(con.getOutputStream());
    StringBuilder out = new StringBuilder();
    out.append("method=" + name + "&");

    if (parameters != null) {
      for (NameValue pair : parameters) {
        out.append(pair.getValue() + "=" + encode(pair.getName()) + "&");
      }
    }

    conout.print(out.toString());
    conout.close();

    InputStream in = con.getInputStream();

    PropBagEx xml = parseInputStream(in);
    String cookie = con.getHeaderField("Set-Cookie");
    if (cookie == null) {
      Map<String, List<String>> headerFields = con.getHeaderFields();
      if (headerFields != null && !Check.isEmpty(headerFields.get("Set-Cookie"))) {
        cookie = headerFields.get("Set-Cookie").get(0);
      }
    }

    xml.setNode("cookie", cookie);

    in.close();

    int buildingBlockDuration = xml.getIntNode("@invocationDuration", -1);
    int thisMethodDuration = (int) ((System.currentTimeMillis() - startTime) / 1000);

    StringBuilder sb = new StringBuilder("URL request from EQUELLA to Blackboard took ");
    sb.append(thisMethodDuration);
    sb.append(" second(s), where ");
    sb.append(buildingBlockDuration);
    sb.append(" second(s) where spent in the Building Block");

    LOGGER.info(sb.toString());

    return xml;
  }

  /**
   * Parses inputstream into a PropBagEx and catches any standard errors that may be contained in
   * the response from the server.
   */
  protected PropBagEx parseInputStream(InputStream in) throws IOException {
    PropBagEx propBag;
    try {
      // Open input stream and receive list of course information
      propBag = new PropBagEx(in);
    } catch (Exception e) {
      throw new IOException("Server could not be reached");
    }

    String error = propBag.getNode("error/@type");
    if (!error.equals("")) {
      // Error message generated by server can also be retrieved
      switch (Integer.parseInt(error)) {
        case 0:
          throw new SecurityException("Username/password incorrect");
        case 1:
          throw new IOException(
              "An error occurred on the Blackboard server: "
                  + propBag.getNode("error/@message")
                  + "\nStack trace: "
                  + propBag.getNode("error"));
        case 2:
          throw new FileNotFoundException("File already exists");
      }
    }
    return propBag;
  }

  protected String ent(String string) {
    StringBuilder szOut = new StringBuilder();
    for (int i = 0; i < string.length(); i++) {
      char ch = string.charAt(i);
      switch (ch) {
        case '<':
          szOut.append("&lt;");
          break;
        case '>':
          szOut.append("&gt;");
          break;
        case '&':
          szOut.append("&amp;");
          break;
        case '"':
          szOut.append("&quot;");
          break;
        default:
          szOut.append(ch);
      }
    }
    return szOut.toString();
  }
}
