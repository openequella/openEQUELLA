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

package com.tle.upgrade.upgraders;

import com.dytech.common.io.ZipUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.util.EquellaConfig;
import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings("nls")
public class UpgradeToEmbeddedTomcat extends AbstractUpgrader {
  public static final String ID = "UpgradeToEmbeddedTomcat";
  public static final Pattern URL_PATT =
      Pattern.compile("^http://([\\.\\w\\-]+)(?::(\\d+))?(/[\\w/]*)?$");

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean canBeRemoved() {
    return false;
  }

  @Override
  @SuppressWarnings("deprecation")
  public List<UpgradeDepends> getDepends() {
    UpgradeDepends dep1 = new UpgradeDepends(UpgradeToTomcat6_0_26.ID);
    dep1.setObsoletes(true);
    UpgradeDepends dep2 = new UpgradeDepends(UpgradeToTomcat6_0_32.ID);
    dep2.setObsoletes(true);
    UpgradeDepends dep3 = new UpgradeDepends(UpgradeToTomcat6_0_35.ID);
    dep3.setObsoletes(true);
    UpgradeDepends dep4 = new UpgradeDepends(AddNonHttpOnly.ID);
    dep4.setObsoletes(true);
    UpgradeDepends dep5 = new UpgradeDepends(UpgradeToTomcat7_0_37.ID);
    dep5.setObsoletes(true);

    // Must run after these when updating from older upgrade
    UpgradeDepends dep6 = new UpgradeDepends(DestroyBIRTEngine.ID);
    UpgradeDepends dep7 = new UpgradeDepends(RemoveClusterGroupName.ID);

    return Arrays.asList(dep1, dep2, dep3, dep4, dep5, dep6, dep7);
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    EquellaConfig config = new EquellaConfig(tleInstallDir);
    File managerDir = config.getManagerDir();

    // Remove Tomcat service (Windows only)
    String platform = ExecUtils.determinePlatform();
    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      execServiceCommand(managerDir, "tomcat", "remove");
    }

    // Remove Tomcat folder
    Path installPath = tleInstallDir.toPath();
    result.addLogMessage("Deleting tomcat directory");
    FileUtils.deleteDirectory(installPath.resolve("tomcat").toFile());

    // Remove Tomcat launch scripts
    result.addLogMessage("Deleting tomcat launch script");
    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      Files.deleteIfExists(installPath.resolve("manager/tomcat.bat"));
    } else {
      Files.deleteIfExists(installPath.resolve("manager/tomcat"));
    }

    // Update mandatory-config.properties for Tomcat
    result.addLogMessage("Updating mandatory-config properties");
    updateMandatoryProperties(result, config.getConfigDir());

    // Update optional-config.properties for ZooKeeper
    result.addLogMessage("Updating optional-config properties");
    updateOptionalProperties(result, config.getConfigDir());

    // Create EQUELLA Server folder
    result.addLogMessage("Creating EQUELLA Server folder structure");
    Path serverPath = installPath.resolve("server");
    Files.deleteIfExists(serverPath);
    Files.createDirectory(serverPath);
    Files.createDirectory(serverPath.resolve("temp"));

    // Copy version.properties to server folder
    result.addLogMessage("Copying version properties");
    InputStream inputStream =
        UpgradeToEmbeddedTomcat.class.getResourceAsStream("/version.properties");
    Files.copy(inputStream, serverPath.resolve("version.properties"));

    // Unzip and copy EQUELLA server jar into
    result.addLogMessage("Deploying new EQUELLA Server jar");
    final File upgradeZip =
        getUpgradeZip(installPath, new File(serverPath.toFile(), "version.properties"));
    File tempDir = File.createTempFile("tle-", "temp");
    tempDir.delete();
    tempDir.mkdir();

    ZipUtils.extract(upgradeZip, tempDir);
    File jarFile = new File(tempDir, "equella-server.jar");
    Files.copy(
        jarFile.toPath(),
        serverPath.resolve("equella-server.jar"),
        StandardCopyOption.REPLACE_EXISTING);

    // Update Manager scripts/configuration and service (Unix/Other only)
    if (!platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      copyResource("/daemon/unix/newmanager", new File(managerDir, "manager"), true);
    }

    // Add EQUELLA Server scripts
    List<String> classPath =
        Lists.newArrayList("../learningedge-config", "../server/equella-server.jar");

    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      String win = ExecUtils.is64Bit(platform) ? "win64" : "win32";
      copyResource(
          "/daemon/" + win + "/equellaserver.bat", new File(managerDir, "equellaserver.bat"), true);

      // Dummy tomcat script to fool the ServiceWrapper
      copyResource(
          "/daemon/" + win + "/equellaserver.bat", new File(managerDir, "tomcat.bat"), true);

      // Rename and update config
      Files.move(
          new File(managerDir, "tomcat-config.bat").toPath(),
          managerDir.toPath().resolve("equellaserver-config.bat"));
      updateConfig(result, managerDir, "equellaserver", classPath, true);

      // Re-install service
      execServiceCommand(managerDir, "equellaserver", "install");
    } else {
      copyResource("/daemon/unix/equellaserver", new File(managerDir, "equellaserver"), true);

      // Dummy tomcat script to fool the ServiceWrapper
      Files.createSymbolicLink(
          installPath.resolve("manager/tomcat"), installPath.resolve("manager/equellaserver"));

      // Rename and update config
      Files.move(
          new File(managerDir, "tomcat-config.sh").toPath(),
          managerDir.toPath().resolve("equellaserver-config.sh"));
      updateConfig(result, managerDir, "equellaserver", classPath, false);
    }

    // Cleanup
    FileUtils.deleteDirectory(tempDir);
  }

  private File getUpgradeZip(Path installPath, File versionProps) throws ConfigurationException {
    final PropertiesConfiguration props = new PropertiesConfiguration(versionProps);
    final String displayName = (String) props.getProperty("version.display");
    final String semanticVersion = displayName.substring(0, displayName.indexOf("-"));
    String filename =
        MessageFormat.format("tle-upgrade-{0} ({1}).zip", semanticVersion, displayName);
    return installPath.resolve("manager/updates/" + filename).toFile();
  }

  private void updateConfig(
      final UpgradeResult result,
      final File managerDir,
      final String name,
      final List<String> classpath,
      boolean windows) {
    final List<String> JMX_CONFIG =
        Lists.newArrayList(
            "-Dcom.sun.management.jmxremote.port=8855",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false");
    final List<String> HEAP_CONFIG =
        Lists.newArrayList("-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=../");

    try {
      if (windows) {
        new LineFileModifier(new File(managerDir, name + "-config.bat"), result) {
          @Override
          protected String processLine(String line) {
            if (line.startsWith("set CLASS_PATH=")) {
              return "set CLASS_PATH=" + StringUtils.join(classpath, ";");
            } else if (line.startsWith("set JAVA_ARGS=")) {
              return updateJavaOpts(line, "set JAVA_ARGS=", ";");
            }
            return line;
          }

          @Override
          protected List<String> addLines() {
            final String heap =
                "rem set HEAP_CONFIG=" + Joiner.on(";").join(HEAP_CONFIG).concat(";");
            final String jmx = "rem set JMX_CONFIG=" + Joiner.on(";").join(JMX_CONFIG).concat(";");

            return Lists.newArrayList(heap, jmx);
          }
        }.update();
      } else {
        new LineFileModifier(new File(managerDir, name + "-config.sh"), result) {
          @Override
          protected String processLine(String line) {
            if (line.startsWith("export CLASSPATH=")) {
              return "export CLASSPATH=\"" + StringUtils.join(classpath, ":") + "\"";
            } else if (line.startsWith("export JAVA_OPTS=")) {
              return updateJavaOpts(line, "export JAVA_OPTS=\"", " ");
            }
            return line;
          }

          @Override
          protected List<String> addLines() {
            final String heap =
                "#export HEAP_CONFIG=\"" + Joiner.on(" ").join(HEAP_CONFIG).concat("\"");
            final String jmx =
                "#export JMX_CONFIG=\"" + Joiner.on(" ").join(JMX_CONFIG).concat("\"");

            return Lists.newArrayList(heap, jmx);
          }
        }.update();
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to update config file", e);
    }
  }

  private String updateJavaOpts(String line, String prefix, String delim) {
    List<String> opts =
        Lists.newArrayList(Splitter.on(delim).split(line.substring(prefix.length())));
    opts.remove("-Dcatalina.base=../tomcat");
    opts.remove("-Dcatalina.home=../tomcat");
    opts.remove("-Djava.io.tmpdir=../tomcat/temp");
    // Add new one to the middle somewhere
    opts.add(2, "-Djava.io.tmpdir=../server/temp");
    return prefix + Joiner.on(delim).join(opts);
  }

  private void execServiceCommand(File managerDir, String script, String command) {
    String serviceCommand = ExecUtils.findExe(new File(managerDir, script)).getAbsolutePath();
    ExecUtils.exec(serviceCommand, command);
  }

  private void updateMandatoryProperties(final UpgradeResult result, File configDir)
      throws ConfigurationException, IOException {
    PropertyFileModifier propMod =
        new PropertyFileModifier(new File(configDir, PropertyFileModifier.MANDATORY_CONFIG)) {
          @Override
          protected boolean modifyProperties(PropertiesConfiguration props) {
            // Add Tomcat ports
            String url = (String) props.getProperty("admin.url");
            String port = getPort(url);
            String tomcatComment =
                System.lineSeparator()
                    + "# Tomcat ports. Specify the ports Tomcat should create connectors for";
            PropertiesConfigurationLayout layout = props.getLayout();
            layout.setLineSeparator(System.lineSeparator());
            if (url.contains("https")) {
              String httpsProp = "https.port";
              props.setProperty(httpsProp, port);
              layout.setComment(httpsProp, tomcatComment);
              props.setProperty("#http.port", "");
            } else {
              String httpProp = "http.port";
              props.setProperty(httpProp, port);
              layout.setComment(httpProp, tomcatComment);
              props.setProperty("#https.port", "");
            }
            props.setProperty("#ajp.port", "8009");

            // Remove Tomcat location
            props.clearProperty("tomcat.location");

            return true;
          }
        };
    propMod.updateProperties();
  }

  private void updateOptionalProperties(final UpgradeResult result, File configDir)
      throws ConfigurationException, IOException {
    // Remove JGroups clustering properties
    File optionalConfig = new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG);
    final Map<String, Object> old = Maps.newHashMap();

    new PropertyFileModifier(optionalConfig) {
      @Override
      protected boolean modifyProperties(PropertiesConfiguration pc) {
        // Get all known properties
        old.put("can.access.internet", pc.getProperty("can.access.internet"));
        old.put("timeZone.default", pc.getProperty("timeZone.default"));
        old.put(
            "conversionService.disableConversion",
            pc.getProperty("conversionService.disableConversion"));
        old.put(
            "conversionService.conversionServicePath",
            pc.getProperty("conversionService.conversionServicePath"));
        old.put("userService.useXForwardedFor", pc.getProperty("userService.useXForwardedFor"));

        old.put("configurationService.proxyHost", pc.getProperty("configurationService.proxyHost"));
        old.put("configurationService.proxyPort", pc.getProperty("configurationService.proxyPort"));
        old.put(
            "configurationService.proxyExceptions",
            pc.getProperty("configurationService.proxyExceptions"));
        old.put(
            "configurationService.proxyUsername",
            pc.getProperty("configurationService.proxyUsername"));
        old.put(
            "configurationService.proxyPassword",
            pc.getProperty("configurationService.proxyPassword"));

        old.put(
            "pluginPathResolver.wrappedClass", pc.getProperty("pluginPathResolver.wrappedClass"));
        old.put("files.useXSendfile", pc.getProperty("files.useXSendfile"));

        old.put("taskService.maxConcurrentTasks", pc.getProperty("taskService.maxConcurrentTasks"));

        old.put(
            "com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval",
            pc.getProperty("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval"));
        old.put(
            "com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval",
            pc.getProperty("com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval"));

        return false;
      }
    }.updateProperties();

    // Delete old config
    Files.deleteIfExists(new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG).toPath());

    // Copy new optional-config in
    copyResource(
        "/config/optional-config.properties",
        new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG));
    // Add existing properties back in
    final Map<String, Object> original =
        ImmutableMap.copyOf(Maps.filterValues(old, Predicates.notNull()));
    LineFileModifier lineMod =
        new LineFileModifier(optionalConfig, result) {
          @Override
          protected String processLine(final String line) {
            for (Entry<String, Object> entry : original.entrySet()) {
              String key = entry.getKey();
              Object value = entry.getValue();
              if (line.contains(key) && value != null) {
                old.remove(key);
                return key + " = " + value;
              }
            }
            return line;
          }

          // write leftovers that don't exist in base
          // optional-config.properties
          @Override
          protected List<String> addLines() {
            List<String> props = Lists.newArrayList();
            if (!Check.isEmpty(old)) {
              for (Entry<String, Object> p : old.entrySet()) {
                if (p.getValue() != null) {
                  props.add(MessageFormat.format("{0} = {1}", p.getKey(), p.getValue()));
                }
              }
            }
            return props;
          }
        };

    lineMod.update();
  }

  private String getPort(String url) {
    String port = "80";
    Matcher m = URL_PATT.matcher(url);
    if (m.matches()) {
      String portStr = m.group(2);
      if (portStr != null) {
        port = portStr;
      }
    }

    return port;
  }
}
