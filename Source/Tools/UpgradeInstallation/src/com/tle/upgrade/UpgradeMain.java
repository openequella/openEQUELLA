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

package com.tle.upgrade;

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.tle.common.Check;
import com.tle.common.util.ExecUtils;
import com.tle.upgrade.UpgradeLog.LogStatus;
import com.tle.upgrade.upgraders.AddExifToolConfg;
import com.tle.upgrade.upgraders.AddFreetextAnalyzerConfig;
import com.tle.upgrade.upgraders.AddLDAPPoolingOptions;
import com.tle.upgrade.upgraders.AddLibAvConfig;
import com.tle.upgrade.upgraders.AddNonHttpOnly;
import com.tle.upgrade.upgraders.AddPostHib5UpgradeConfig;
import com.tle.upgrade.upgraders.ConvertBoneCPtoHikariCP;
import com.tle.upgrade.upgraders.ConvertC3P0ToBoneCP;
import com.tle.upgrade.upgraders.CreateUpgraderLog4j;
import com.tle.upgrade.upgraders.DeleteLuceneIndex;
import com.tle.upgrade.upgraders.DestroyBIRTEngine;
import com.tle.upgrade.upgraders.DestroyBIRTEngineOld;
import com.tle.upgrade.upgraders.HashManagerPassword;
import com.tle.upgrade.upgraders.ModifyBirtConfig;
import com.tle.upgrade.upgraders.MovePluginOptions;
import com.tle.upgrade.upgraders.RemoveClusterGroupName;
import com.tle.upgrade.upgraders.RemoveQuartzPropertiesFile;
import com.tle.upgrade.upgraders.RenameBehindProxyConfig;
import com.tle.upgrade.upgraders.UpdateClusterConfig;
import com.tle.upgrade.upgraders.UpdateHibernateProperties;
import com.tle.upgrade.upgraders.UpdateLog4jConfigForTomcatLog;
import com.tle.upgrade.upgraders.UpdateManagerJar;
import com.tle.upgrade.upgraders.UpdateServiceWrapper;
import com.tle.upgrade.upgraders.UpdateToApacheCommonsDaemon;
import com.tle.upgrade.upgraders.UpgradeBIRTEngine;
import com.tle.upgrade.upgraders.UpgradeToEmbeddedTomcat;
import com.tle.upgrade.upgraders.UpgradeToTomcat6_0_26;
import com.tle.upgrade.upgraders.UpgradeToTomcat6_0_32;
import com.tle.upgrade.upgraders.UpgradeToTomcat6_0_35;
import com.tle.upgrade.upgraders.UpgradeToTomcat7_0_37;
import com.tle.upgrade.upgraders.apachedaemon.UpdateApacheDaemon;
import com.tle.upgrade.upgraders.ffmpeg.AddFfmpegConfig;
import com.tle.upgrade.upgraders.java17.UpdateJavaOpts;
import com.tle.upgrade.upgraders.log4j2.UpdateLog4JConfigFile;
import com.tle.upgrade.upgraders.v20252.AddKeepaliveAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
public class UpgradeMain {
  static {
    URL log4jConfigFile =
        ClassLoader.getSystemResource("com/tle/upgrade/upgraders/upgrader-log4j.yaml");
    if (log4jConfigFile != null) {
      System.getProperties().setProperty("log4j2.configurationFile", log4jConfigFile.toString());
    }
  }

  private static final Log LOGGER = LogFactory.getLog(UpgradeMain.class);

  private static String commit = "476-g5014b34";
  private File configDir;
  private File tleInstallDir;

  private final XStream xstream;

  private final File upgradeLogFile;
  private static boolean offline;

  public static Upgrader[] upgraders =
      new Upgrader[] {
        new UpdateLog4jConfigForTomcatLog(),
        new UpdateHibernateProperties(),
        new UpdateManagerJar(),
        new UpdateServiceWrapper(),
        new UpdateClusterConfig(),
        new MovePluginOptions(),
        new ModifyBirtConfig(),
        new UpgradeToTomcat6_0_26(),
        new UpgradeToTomcat6_0_32(),
        new UpgradeBIRTEngine(),
        new DestroyBIRTEngineOld(),
        new DestroyBIRTEngine(),
        new RenameBehindProxyConfig(),
        new CreateUpgraderLog4j(),
        new HashManagerPassword(),
        new UpdateToApacheCommonsDaemon(),
        new RemoveClusterGroupName(),
        new RemoveQuartzPropertiesFile(),
        new UpgradeToTomcat6_0_35(),
        new ConvertC3P0ToBoneCP(),
        new DeleteLuceneIndex(),
        new UpgradeToTomcat7_0_37(),
        new AddNonHttpOnly(),
        new UpgradeToEmbeddedTomcat(),
        new AddExifToolConfg(),
        new ConvertBoneCPtoHikariCP(),
        new AddLDAPPoolingOptions(),
        new AddLibAvConfig(),
        new AddFreetextAnalyzerConfig(),
        new AddPostHib5UpgradeConfig(),
        new UpdateLog4JConfigFile(),
        new AddFfmpegConfig(),
        new UpdateApacheDaemon(),
        new UpdateJavaOpts(),
        new AddKeepaliveAttribute()
      };

  public static void main(String[] args) throws Throwable {
    try {
      LOGGER.info("Upgrader started");
      InputStream verStream = UpgradeMain.class.getResourceAsStream("/version.properties");
      if (verStream != null) {
        Properties props = new Properties();
        props.load(verStream);
        commit = props.getProperty("version.commit");
      }
      offline = Boolean.getBoolean("equella.offline");
      String installDir = System.getProperty("equella.install.directory");
      if (installDir == null) {
        File folder = ExecUtils.findJarFolder(UpgradeMain.class);
        installDir = folder.getParent();
      }
      boolean install = false;
      for (String arg : args) {
        if (arg.equals("--install")) {
          install = true;
        }
      }
      UpgradeMain upgrader = new UpgradeMain(new File(installDir));
      if (install) {
        upgrader.install();
      } else {
        upgrader.upgrade();
      }
    } catch (Exception t) {
      LOGGER.error("Error running database-upgrader.jar", t);
      System.exit(1);
    }
  }

  public static boolean isOffline() {
    return offline;
  }

  public UpgradeMain(File path) {
    if (path.getName().equals(Constants.LEARNINGEDGE_CONFIG_FOLDER)) {
      configDir = path;
      tleInstallDir = path.getParentFile();
    } else {
      tleInstallDir = path;
      configDir = new File(tleInstallDir, Constants.LEARNINGEDGE_CONFIG_FOLDER);
    }
    if (!configDir.isDirectory()) {
      throw new Error(
          "Path '" + tleInstallDir + "' does not appear to be an EQUELLA install directory");
    }
    xstream = new XStream();
    xstream.allowTypes(new Class[] {com.tle.upgrade.UpgradeLog.class});
    upgradeLogFile = new File(configDir, "upgrade-log.xml");
  }

  private void install() throws Exception {
    if (upgradeLogFile.exists()) {
      LOGGER.info("upgrade-log.xml already installed");
    }
    List<UpgradeLog> allEntries = Lists.newArrayList();
    Date now = new Date();
    for (Upgrader upgrader : upgraders) {
      if (!upgrader.isRunOnInstall()) {
        String id = upgrader.getId();
        UpgradeLog entry = new UpgradeLog();
        entry.setMustExist(!upgrader.isBackwardsCompatible());
        entry.setStatus(LogStatus.SKIPPED);
        entry.setExecuted(new Date(now.getTime()));
        entry.setMigrationId(id);
        allEntries.add(entry);
      }
    }
    saveUpgradeLog(allEntries);
    upgrade();
  }

  @SuppressWarnings({"unchecked"})
  private void upgrade() throws Exception {
    List<UpgradeLog> logEntries;
    if (upgradeLogFile.exists()) {
      UnicodeReader reader = new UnicodeReader(new FileInputStream(upgradeLogFile), "UTF-8");
      try {
        logEntries = (List<UpgradeLog>) xstream.fromXML(reader);
      } finally {
        reader.close();
      }
    } else {
      logEntries = Lists.newArrayList();
    }

    Map<String, UpgradeLog> statuses = new HashMap<String, UpgradeLog>();
    for (UpgradeLog logEntry : logEntries) {
      statuses.put(logEntry.getMigrationId(), logEntry);
    }
    Map<String, UpgradeOperation> extensionMap = new HashMap<String, UpgradeOperation>();
    for (Upgrader upgrader : upgraders) {
      String id = upgrader.getId();
      UpgradeOperation migrateExtension = new UpgradeOperation(id);
      migrateExtension.setUpgrader(upgrader);
      UpgradeLog entry = statuses.remove(id);
      if (entry != null) {
        migrateExtension.setLogEntry(entry);
      }
      extensionMap.put(id, migrateExtension);
    }
    for (UpgradeLog log : statuses.values()) {
      if (log.isMustExist()) {
        throw new RuntimeException(
            "Can not upgrade to this version, missing required backwards incompatible migration: "
                + log.getMigrationId());
      }
    }
    List<UpgradeOperation> toProcess = new ArrayList<UpgradeOperation>();
    for (String migrationId : extensionMap.keySet()) {
      processMigration(migrationId, toProcess, extensionMap);
    }
    executeUpgrades(toProcess, logEntries);
    saveUpgradeLog(logEntries);
  }

  private void saveUpgradeLog(List<UpgradeLog> entries) throws IOException {
    File bakFile = new File(configDir, "upgrade-log.xml.bak");
    if (upgradeLogFile.exists()) {
      try {
        new FileCopier(upgradeLogFile, bakFile, true).rename();
      } catch (Exception e) {
        throw new IOException("Could not back up existing upgrade-log.xml", e);
      }
    }

    try {
      try (OutputStreamWriter writer =
          new OutputStreamWriter(new FileOutputStream(upgradeLogFile), "UTF-8")) {
        xstream.toXML(entries, writer);
        FileUtils.delete(bakFile);
      }
    } catch (Exception t) {
      FileUtils.delete(upgradeLogFile);
      new FileCopier(bakFile, upgradeLogFile, false).rename();
    }
  }

  private void executeUpgrades(List<UpgradeOperation> toProcess, List<UpgradeLog> logEntries)
      throws Exception {
    for (UpgradeOperation upgradeOperation : toProcess) {
      Upgrader upgrader = upgradeOperation.getUpgrader();
      UpgradeLog log = upgradeOperation.getLogEntry();
      if (log == null) {
        log = new UpgradeLog();
        log.setMigrationId(upgradeOperation.getId());
        logEntries.add(log);
      }

      log.setExecuted(new Date());
      log.setMustExist(!upgrader.isBackwardsCompatible());
      if (upgradeOperation.isSkip()) {
        log.setStatus(LogStatus.SKIPPED);
      } else {
        LOGGER.debug("Executing upgrader: '" + upgradeOperation.getId() + "'");
        UpgradeResult result = new UpgradeResult(LOGGER);
        try {
          upgrader.upgrade(result, tleInstallDir);
          log.setLog(result.getWorkLog());
          if (result.isRetry()) {
            log.setCanRetry(true);
            log.setStatus(LogStatus.ERRORED);
          } else {
            log.setStatus(LogStatus.EXECUTED);
          }
          log.setMessage(result.getMessage());
          log.setErrorMessage(null);
        } catch (Exception t) {
          log.setLog(result.getWorkLog());
          StringWriter strWriter = new StringWriter();
          t.printStackTrace(new PrintWriter(strWriter));
          String errorMsg = strWriter.toString();
          String msg = result.getMessage();
          if (Check.isEmpty(result.getMessage())) {
            msg = t.getMessage();
          }
          log.setMessage(msg);
          log.setErrorMessage(errorMsg);
          log.setStatus(LogStatus.ERRORED);
          LOGGER.error("Error running " + upgrader.getClass(), t);

          throw new RuntimeException(t);
        }
      }
    }
  }

  private void processMigration(
      String id, List<UpgradeOperation> toProcess, Map<String, UpgradeOperation> extensionMap) {
    UpgradeOperation extension = extensionMap.get(id);
    if (!extension.isProcessed()) {
      extension.setProcessed(true);
      if (extension.getStatus() == null || (extension.getStatus() == LogStatus.ERRORED)) {
        List<UpgradeDepends> depends = extension.getUpgrader().getDepends();
        for (UpgradeDepends depend : depends) {
          UpgradeOperation dependency = extensionMap.get(depend.getId());
          if (depend.isObsoletes() && dependency.getStatus() == null) {
            dependency.setSkip(true);
          }
          if (depend.isFixes() && dependency.getStatus() != LogStatus.EXECUTED) {
            extension.setSkip(true);
          }
          processMigration(depend.getId(), toProcess, extensionMap);
        }
        toProcess.add(extension);
      }
    }
  }

  public static class UpgradeOperation {
    private final String id;
    private Upgrader upgrader;
    private UpgradeLog logEntry;
    private boolean skip;
    private boolean processed;

    public UpgradeOperation(String id) {
      this.id = id;
    }

    public boolean isCanRetry() {
      return logEntry != null && logEntry.isCanRetry();
    }

    public LogStatus getStatus() {
      if (logEntry == null) {
        return null;
      }
      return logEntry.getStatus();
    }

    public boolean isSkip() {
      return skip;
    }

    public void setSkip(boolean skip) {
      this.skip = skip;
    }

    public boolean isProcessed() {
      return processed;
    }

    public void setProcessed(boolean processed) {
      this.processed = processed;
    }

    public String getId() {
      return id;
    }

    public UpgradeLog getLogEntry() {
      return logEntry;
    }

    public void setLogEntry(UpgradeLog logEntry) {
      this.logEntry = logEntry;
    }

    public Upgrader getUpgrader() {
      return upgrader;
    }

    public void setUpgrader(Upgrader upgrader) {
      this.upgrader = upgrader;
    }
  }

  public static String getCommit() {
    return commit;
  }
}
