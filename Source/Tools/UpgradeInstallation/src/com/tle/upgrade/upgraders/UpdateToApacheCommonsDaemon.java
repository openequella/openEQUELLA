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

import com.tle.common.Check;
import com.tle.common.util.EquellaConfig;
import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpdateToApacheCommonsDaemon extends AbstractUpgrader {
  private static final String VERSION_FILE =
      "tomcat/webapps/ROOT/version.properties"; //$NON-NLS-1$
  private static final Log LOGGER = LogFactory.getLog(UpdateToApacheCommonsDaemon.class);

  @Override
  public String getId() {
    return "UpdateToApacheCommonsDaemon"; //$NON-NLS-1$
  }

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  @Override
  public List<UpgradeDepends> getDepends() {
    UpgradeDepends depends = new UpgradeDepends("UpdateServiceWrapper");
    depends.setObsoletes(true);
    return Arrays.asList(depends);
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    EquellaConfig config = new EquellaConfig(tleInstallDir);
    File managerDir = config.getManagerDir();
    File offlineFile = new File(managerDir, "runoffline"); // $NON-NLS-1$
    if (!offlineFile.exists()) {
      setupOffline(result, managerDir, tleInstallDir, offlineFile);
      return;
    }
    restoreOffline(offlineFile, managerDir, tleInstallDir);

    String platform = ExecUtils.determinePlatform();
    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      execServiceCommand(managerDir, "remove", false); // $NON-NLS-1$
      execServiceCommand(managerDir, "remove", true); // $NON-NLS-1$
    } else {
      execServiceCommand(managerDir, "stop", false); // $NON-NLS-1$
    }

    copyFiles(platform, managerDir);

    String VersionMm = getMajorVersion(tleInstallDir);

    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      updateFiles(result, managerDir, "tomcat", true, VersionMm); // $NON-NLS-1$
      updateFiles(result, managerDir, "manager", true, VersionMm); // $NON-NLS-1$
      execServiceCommand(managerDir, "install", false); // $NON-NLS-1$
      execServiceCommand(managerDir, "install", true); // $NON-NLS-1$
    } else {
      updateFiles(result, managerDir, "tomcat", false, VersionMm); // $NON-NLS-1$
      updateFiles(result, managerDir, "manager", false, VersionMm); // $NON-NLS-1$
    }

    moveOldFiles(result, managerDir);
    execServiceCommand(managerDir, "start", false); // $NON-NLS-1$
  }

  @SuppressWarnings("nls")
  private void moveOldFiles(UpgradeResult result, File managerDir) {
    File oldFolder = new File(managerDir, "old-wrapper");
    if (!oldFolder.exists()) {
      oldFolder.mkdir();
    }

    String platform = ExecUtils.determinePlatform();
    if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      renameIfExists(new File(managerDir, "wrapper.exe"), new File(oldFolder, "wrapper.exe"));
      renameIfExists(new File(managerDir, "wrapper.dll"), new File(oldFolder, "wrapper.dll"));
    } else {
      renameIfExists(new File(managerDir, "wrapper"), new File(oldFolder, "wrapper"));
      renameIfExists(new File(managerDir, "libwrapper.so"), new File(oldFolder, "libwrapper.so"));
    }

    if (platform.startsWith(ExecUtils.PLATFORM_MAC)) {
      renameIfExists(
          new File(managerDir, "libwrapper.jnilib"), new File(oldFolder, "libwrapper.jnilib"));
    }

    renameIfExists(new File(managerDir, "manager.conf"), new File(oldFolder, "manager.conf"));
    renameIfExists(new File(managerDir, "tomcat.conf"), new File(oldFolder, "tomcat.conf"));
    renameIfExists(new File(managerDir, "wrapper.jar"), new File(oldFolder, "wrapper.jar"));
  }

  private void updateFiles(
      final UpgradeResult result,
      final File managerDir,
      final String name,
      final boolean windows,
      final String versionMM)
      throws IOException, ConfigurationException {
    new PropertyFileModifier(new File(managerDir, name + ".conf")) // $NON-NLS-1$
    {
      @SuppressWarnings("nls")
      @Override
      protected boolean modifyProperties(PropertiesConfiguration props) {
        int i = 1;
        String javaArg = props.getString("wrapper.java.additional." + i++);
        final Set<String> javaArgs = new LinkedHashSet<String>();
        while (!Check.isEmpty(javaArg)) {
          javaArgs.add(javaArg);
          javaArg = props.getString("wrapper.java.additional." + i++);
        }

        // Remove args we no longer need
        javaArgs.remove("-server");
        javaArgs.remove("-Demma.rt.control=false");

        // Increase PermSize (See #5881)
        javaArgs.remove("-XX:MaxPermSize=128m");
        javaArgs.add("-XX:MaxPermSize=256m");

        // Add new args to make us better, faster, stronger!
        javaArgs.add("-XX:MaxGCPauseMillis=500");
        javaArgs.add("-XX:NewRatio=3");
        javaArgs.add("-XX:GCTimeRatio=16");
        javaArgs.add("-XX:+DisableExplicitGC");
        javaArgs.add("-XX:+UseConcMarkSweepGC");
        javaArgs.add("-XX:+UseParNewGC");
        javaArgs.add("-XX:CMSInitiatingOccupancyFraction=70");

        i = 1;
        String classpath = props.getString("wrapper.java.classpath." + i++);
        final ArrayList<String> classpaths = new ArrayList<String>();
        while (!Check.isEmpty(classpath)) {
          if (!classpath.endsWith("lib/tools.jar") && !classpath.equals("./wrapper.jar")) {
            classpaths.add(classpath);
          }
          classpath = props.getString("wrapper.java.classpath." + i++);
        }

        if (windows) {
          // add tomcat-juli.jar
          classpaths.add("../tomcat/bin/tomcat-juli.jar");

          // On 4.0 the config files for windows have paths with
          // single backslashes. This means that when they are read
          // they are interpreted as escape characters resulting in
          // an invalid path
          String javaHome;
          try {
            String propertyFileContents = FileUtils.readFileToString(props.getFile());
            PropertiesConfiguration windowsProp = new PropertiesConfiguration();
            windowsProp.load(new StringReader(propertyFileContents.replace("\\", "/")));
            javaHome = windowsProp.getString("wrapper.java.command");
          } catch (IOException e) {
            javaHome = props.getString("wrapper.java.command");
          } catch (ConfigurationException e) {
            javaHome = props.getString("wrapper.java.command");
          }

          if (javaHome.contains(".exe")) {
            javaHome = javaHome.replace("/bin/java.exe", "");
          } else {
            javaHome = javaHome.replace("/bin/java", "");
          }
          String serviceName = props.getString("wrapper.ntservice.name").replaceAll("\\s", "");
          String displayName = props.getString("wrapper.ntservice.displayname");
          final boolean autoStart =
              props.getString("wrapper.ntservice.starttype").equals("AUTO_START");

          if (versionMM != null && versionMM.matches("\\d+\\.\\d+")) {
            // If we were able to identify a version number of the
            // expected num-dot-num, and if the existing service
            // or its display name contain a num-dot-num, we
            // update the identifier & display strings
            if (serviceName.matches(".*\\d+\\.\\d+.*")) {
              String oldName = serviceName;
              LOGGER.info("Reconfiguring " + oldName + " as " + serviceName);
              serviceName = serviceName.replaceFirst("\\d+\\.\\d+", versionMM);
            }
            if (displayName.matches(".*\\d+\\.\\d+.*")) {
              String oldName = displayName;
              LOGGER.info("Reconfiguring " + oldName + " as " + displayName);
              displayName = displayName.replaceFirst("\\d+\\.\\d+", versionMM);
            }
          }

          updateWinConfig(
              result,
              managerDir,
              name,
              javaHome,
              javaArgs,
              classpaths,
              serviceName,
              displayName,
              autoStart);
        } else {
          String javaHome = props.getString("wrapper.java.command").replace("/bin/java", "");
          updateUnixConfig(result, managerDir, name, javaHome, javaArgs, classpaths);
        }
        return false;
      }
    }.updateProperties();
  }

  @SuppressWarnings("nls")
  private void updateUnixConfig(
      final UpgradeResult result,
      final File managerDir,
      final String name,
      final String javaHome,
      final Collection<String> javaArgs,
      final ArrayList<String> classpaths) {
    try {
      new LineFileModifier(new File(managerDir, name + "-config.sh"), result) {

        @Override
        protected String processLine(String line) {
          if (line.startsWith("export JAVA_OPTS=")) {
            return "export JAVA_OPTS=\"" + StringUtils.join(javaArgs, " ") + "\"";
          } else if (line.startsWith("export CLASSPATH=")) {
            return "export CLASSPATH=\"" + StringUtils.join(classpaths, ":") + "\"";
          } else if (line.startsWith("export JAVA_HOME=")) {
            return "export JAVA_HOME=\"" + javaHome + "\"";
          } else {
            return line;
          }
        }
      }.update();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update " + name + "-config.sh file", e);
    }
  }

  @SuppressWarnings("nls")
  private void updateWinConfig(
      final UpgradeResult result,
      final File managerDir,
      final String name,
      final String javaHome,
      final Collection<String> javaArgs,
      final ArrayList<String> classpaths,
      final String serviceName,
      final String displayName,
      final boolean autoStart) {
    try {
      new LineFileModifier(new File(managerDir, name + "-config.bat"), result) {
        @Override
        protected String processLine(String line) {
          if (line.startsWith("set JAVA_ARGS=")) {
            return "set JAVA_ARGS=" + StringUtils.join(javaArgs, ";");
          } else if (line.startsWith("set CLASS_PATH=")) {
            return "set CLASS_PATH=" + StringUtils.join(classpaths, ";");
          } else if (line.startsWith("set DISPLAY_NAME=")) {
            return "set DISPLAY_NAME=" + displayName;
          } else if (line.startsWith("set JAVA_HOME=")) {
            return "set JAVA_HOME=" + javaHome;
          } else if (line.startsWith("set SERVICE_NAME=") && !line.contains("%SERVICE_NAME")) {
            return "set SERVICE_NAME=" + serviceName;
          } else if (line.startsWith("set START_TYPE=")) {
            if (autoStart) {
              return "set START_TYPE=auto";
            } else {
              return "set START_TYPE=maunal";
            }
          } else {
            return line;
          }
        }
      }.update();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update bat file " + name + "-config.bat", e);
    }
  }

  @SuppressWarnings("nls")
  private void copyFiles(String platform, File managerDir) {
    if (ExecUtils.isPlatformUnix(platform)) {
      copyResource("/daemon/unix/tomcat", managerDir, true);
      copyResource("/daemon/unix/manager", managerDir, true);
      copyResource("/daemon/unix/tomcat-config.sh", managerDir, true);
      copyResource("/daemon/unix/manager-config.sh", managerDir, true);
      copyResource("/daemon/" + platform + "/jsvc", managerDir, true);
    } else if (platform.startsWith(ExecUtils.PLATFORM_WIN)) {
      String win = ExecUtils.is64Bit(platform) ? "win64" : "win32";
      copyResource("/daemon/" + win + "/tomcat.bat", managerDir);
      copyResource("/daemon/" + win + "/tomcat-config.bat", managerDir);
      copyResource("/daemon/" + win + "/manager.bat", managerDir);
      copyResource("/daemon/" + win + "/manager-config.bat", managerDir);
      copyResource("/daemon/" + win + "/prunsrv.exe", managerDir);
      copyResource("/daemon/" + win + "/prunmgr.exe", managerDir);
    }
  }

  private void restoreOffline(File offlineFile, File managerDir, File tleInstallDir) {
    deleteFile(offlineFile);
    File bakFile = new File(tleInstallDir, VERSION_FILE + ".bak"); // $NON-NLS-1$
    File oldVerFile = new File(tleInstallDir, VERSION_FILE);
    deleteFile(oldVerFile);
    rename(bakFile, oldVerFile);
    File tomcatConf = new File(managerDir, "old.tomcat.conf"); // $NON-NLS-1$
    rename(tomcatConf, new File(managerDir, tomcatConf.getName().substring(4)));

    if (ExecUtils.isPlatformUnix(ExecUtils.determinePlatform())) {
      File manager = new File(managerDir, "manager"); // $NON-NLS-1$
      deleteFile(manager);
      File origManager = new File(managerDir, "manager.orig"); // $NON-NLS-1$
      rename(origManager, manager);
    }
  }

  @SuppressWarnings("nls")
  private void deleteFile(File file) {
    if (!file.delete()) {
      throw new RuntimeException("Failed to delete " + file);
    }
  }

  @SuppressWarnings("nls")
  private void setupOffline(
      UpgradeResult result, File managerDir, File tleInstallDir, File offlineFile)
      throws Exception {
    result.setRetry(true);
    File bakFile = new File(tleInstallDir, VERSION_FILE + ".bak");
    if (!bakFile.exists()) {
      File versionFile = new File(tleInstallDir, VERSION_FILE);
      rename(versionFile, bakFile);
      Properties fakeProps = new Properties();

      try (FileInputStream finp = new FileInputStream(bakFile);
          FileOutputStream out = new FileOutputStream(versionFile)) {
        fakeProps.load(finp);
        fakeProps.setProperty(
            "version.major",
            "Restart manager to continue. " + fakeProps.getProperty("version.major"));
        fakeProps.store(out, null);
      }
    }
    File tomcatConf = new File(managerDir, "tomcat.conf");
    if (tomcatConf.exists()) {
      File newName = new File(managerDir, "old." + tomcatConf.getName());
      rename(tomcatConf, newName);
    }
    if (!offlineFile.exists()) {
      FileOutputStream fout = new FileOutputStream(offlineFile);
      fout.close();
    }

    if (ExecUtils.isPlatformUnix(ExecUtils.determinePlatform())) {
      File manager = new File(managerDir, "manager");
      File origManager = new File(managerDir, "manager.orig");
      rename(manager, origManager);
      copyResource("/daemon/unix/restartManager", managerDir, true);
      rename(new File(managerDir, "restartManager"), manager);
    }
  }

  private String getMajorVersion(File tleInstallDir) {
    String upgradedToVersionMajor = null;
    File versionFile = new File(tleInstallDir, VERSION_FILE);
    Properties fakeProps = new Properties();

    try (FileInputStream finp = new FileInputStream(versionFile)) {
      fakeProps.load(finp);
      upgradedToVersionMajor = fakeProps.getProperty("version.mm");
    } catch (IOException fnfe) {

    }
    return upgradedToVersionMajor;
  }

  @SuppressWarnings("nls")
  private void execServiceCommand(File managerDir, String command, boolean tomcat) {
    String serviceCommand =
        ExecUtils.findExe(new File(managerDir, tomcat ? "tomcat" : "manager")).getAbsolutePath();
    ExecUtils.exec(serviceCommand, command);
  }
}
