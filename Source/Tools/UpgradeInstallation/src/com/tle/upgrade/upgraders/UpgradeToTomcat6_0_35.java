/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.upgrade.upgraders;

import com.google.common.io.ByteStreams;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obsolete
 *
 * @author aholland
 */
@Deprecated
@SuppressWarnings("nls")
public class UpgradeToTomcat6_0_35 extends AbstractTomcatUpgrader {
  public static final String ID = "UpgradeTomcat6_0_35";

  private static final Pattern PROTOCOL_REGEX = Pattern.compile("protocol=\"(HTTP/1\\.1)\"");
  private static final String NIO_PROTOCOL =
      "protocol=\"org.apache.coyote.http11.Http11NioProtocol\"";
  private static final String SERVER_XML = "server.xml";
  private static final String CONF = "conf";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  protected String getTomcatZip() {
    return "tomcat-6.0.35.zip";
  }

  @Override
  protected String getDefaultUpgradeUrl() {
    return "http://support.thelearningedge.com.au/downloads/5.1/Upgrade%20Files/";
  }

  @Override
  public List<UpgradeDepends> getDepends() {
    UpgradeDepends dep1 = new UpgradeDepends(UpgradeToTomcat6_0_26.ID);
    dep1.setObsoletes(true);
    UpgradeDepends dep2 = new UpgradeDepends(UpgradeToTomcat6_0_32.ID);
    dep2.setObsoletes(true);

    return Arrays.asList(dep1, dep2);
  }

  @Override
  protected void afterTomcatExtraction(
      UpgradeResult result, File tleInstallDir, File tomcatFolder, File tomcatBackupFolder)
      throws Exception {
    super.afterTomcatExtraction(result, tleInstallDir, tomcatFolder, tomcatBackupFolder);

    // copy old server.xml and replace HTTP protocol with Nio
    File newServerXml = new File(new File(tomcatFolder, CONF), SERVER_XML);
    File oldServerXml = new File(new File(tomcatBackupFolder, CONF), SERVER_XML);

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (InputStream in = new BufferedInputStream(new FileInputStream(oldServerXml));
        OutputStream out = new BufferedOutputStream(bytes)) {
      ByteStreams.copy(in, out);
    }

    String text = new String(bytes.toByteArray());
    final Matcher matcher = PROTOCOL_REGEX.matcher(text);
    if (matcher.find()) {
      text = matcher.replaceAll(NIO_PROTOCOL);
      result.info("Replaced HTTP protocol with Nio protocol");
    }
    result.info("Copying " + SERVER_XML);

    try (InputStream in = new BufferedInputStream(new ByteArrayInputStream(text.getBytes()));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(newServerXml))) {
      ByteStreams.copy(in, out);
    }
  }
}
