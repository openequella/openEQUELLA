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

package com.tle.web.inplaceeditor;

import com.dytech.edge.exceptions.FileSystemException;
import com.sun.jna.Platform;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.applet.client.ClientProxyFactory;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.inplaceeditor.InPlaceEditorServerBackend;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.appletcommon.AbstractAppletLauncher;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.appletcommon.io.ProgressMonitorCallback;
import com.tle.web.appletcommon.io.ProgressMonitorInputStream;
import com.tle.web.appletcommon.io.ProgressMonitorOutputStream;
import com.tle.web.inplaceeditor.win32.WindowsOpener;
import java.awt.AWTPermission;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("nls")
public class InPlaceEditAppletLauncher extends AbstractAppletLauncher implements ActionListener {
  private static final String DEBUG = PARAMETER_PREFIX + "DEBUG";
  private static final String FILENAME = PARAMETER_PREFIX + "FILENAME";
  private static final String MIMETYPE = PARAMETER_PREFIX + "MIMETYPE";
  private static final String OPENWITH = PARAMETER_PREFIX + "OPENWITH";
  private static final String SERVICE = PARAMETER_PREFIX + "SERVICE";
  private static final String STAGINGID = PARAMETER_PREFIX + "STAGINGID";
  private static final String ITEMUUID = PARAMETER_PREFIX + "ITEMUUID";
  private static final String ITEMVERSION = PARAMETER_PREFIX + "ITEMVERSION";
  private static final String BACKGROUND = PARAMETER_PREFIX + "BACKGROUND";
  private static final String CROSSDOMAIN = PARAMETER_PREFIX + "CROSSDOMAIN";
  private static final String INSTANCEID = PARAMETER_PREFIX + "INSTANCEID";

  private static final int SYNC_TIME = 1000; // we can be quite aggressive
  // about this
  /** Looking to optimise the download/upload times, especially where progress bars are concerned */
  private static final int STREAM_BUF_SIZE = 32768;

  private InPlaceEditorServerBackend server;

  private final String appletId;

  // Passed in
  private String service;
  private String stagingId;
  private String itemUuid;
  private int itemVersion;
  private boolean openWith;
  private String filename;
  private String mimetype;
  private boolean debug;
  /** Sigh: http://dev.equella.com/issues/6395 */
  private String instanceid;

  private String filepath;

  private CachedFile file;
  private boolean synchronising;
  private Timer fileWatchTimer;
  private final Opener opener;

  private JButton saveButton;
  private JButton ignoreChangesButton;

  /**
   * Since hacky timers are used when doing LiveConnect calls, we need to wait for the results to
   * become available
   */
  private final Object resultLock = new Object();

  private Boolean pending;
  private Boolean syncing;

  public InPlaceEditAppletLauncher() {
    appletId = UUID.randomUUID().toString();
    logger.info("Created " + this.getClass().getSimpleName() + " with ID " + appletId);
    opener = getOpener();
  }

  private Opener getOpener() {
    if (Platform.isWindows()) {
      final Permissions permissions = new Permissions();
      permissions.add(new AllPermission());
      final AccessControlContext context =
          new AccessControlContext(
              new ProtectionDomain[] {new ProtectionDomain(null, permissions)});
      return AccessController.doPrivileged(
          new PrivilegedAction<Opener>() {
            @Override
            public Opener run() {
              return new WindowsOpener();
            }
          },
          context);
    }

    if (Platform.isLinux()) {
      return new LinuxOpener();
    }

    if (Platform.isMac()) {
      return new MacOpener();
    }

    throw new UnsupportedOperationException("This platform is not supported");
  }

  @Override
  protected JComponent initAndCreateRootComponent() throws Exception {
    initSettings();
    initServer();

    saveButton = new JButton(CurrentLocale.get("button.save"));
    saveButton.addActionListener(new SaveActionListener());
    saveButton.setVisible(false);

    ignoreChangesButton = new JButton(CurrentLocale.get("button.cancel"));
    ignoreChangesButton.addActionListener(new CancelActionListener());
    ignoreChangesButton.setVisible(false);

    final JPanel all = new JPanel();
    all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    all.setBackground(getBackgroundColour());

    all.add(saveButton);
    all.add(ignoreChangesButton);

    return all;
  }

  private Color getBackgroundColour() {
    String backgroundColour = getParameter(BACKGROUND);
    debug("Background colour " + backgroundColour);

    int[] rgb = new int[] {255, 255, 255};
    if (!Check.isEmpty(backgroundColour)) {
      try {
        if (backgroundColour.startsWith("rgb(") || backgroundColour.startsWith("rgba(")) {
          debug("starts with rgb[a](");
          backgroundColour =
              backgroundColour.substring(
                  (backgroundColour.startsWith("rgba(") ? 5 : 4), backgroundColour.length() - 1);
          String[] components = backgroundColour.split(",");
          rgb =
              new int[] {
                Integer.parseInt(components[0].trim()),
                Integer.parseInt(components[1].trim()),
                Integer.parseInt(components[2].trim())
              };
        }
        // IE
        else if (backgroundColour.startsWith("#")) {
          int hexDigits = 2;
          if (backgroundColour.length() == 4) {
            hexDigits = 1;
          }

          rgb =
              new int[] {
                Integer.parseInt(backgroundColour.substring(1, 1 + hexDigits), 16),
                Integer.parseInt(
                    backgroundColour.substring(1 + 1 * +hexDigits, 1 + 2 * +hexDigits), 16),
                Integer.parseInt(
                    backgroundColour.substring(1 + 2 * +hexDigits, 1 + 3 * +hexDigits), 16)
              };
        }
        debug("component ints = " + rgb[0] + ", " + rgb[1] + ", " + rgb[2]);
      } catch (Exception e) {
        // Not fatal, don't throw it
        logException("Error setting background colour", e);
      }
    }
    return new Color(rgb[0], rgb[1], rgb[2]);
  }

  @Override
  protected void onFinished() {
    super.onFinished();

    final File lock = new File(System.getProperty("java.io.tmpdir"), instanceid + ".lock");
    if (lock.exists()) {
      logger.log(Level.INFO, "Lock file " + lock.getAbsolutePath() + " exists, doing nothing.");
      return;
    }
    try {
      lock.createNewFile();
    } catch (IOException e1) {
      logger.log(Level.SEVERE, "Error creating lock file", e1);
    }

    final Pair<InputStream, Integer> remoteFile = readFileFromServer();

    final GlassProgressWorker<?> progWorker =
        new GlassProgressWorker<File>(
            CurrentLocale.get("label.downloading"), remoteFile.getSecond(), false) {
          @Override
          public File construct() throws Exception {
            synchronising = false;
            file = null;

            File tempFile = new File(filepath);
            tempFile.deleteOnExit();
            logger.info("file size to download is " + remoteFile.getSecond());
            try (InputStream in = remoteFile.getFirst();
                OutputStream out =
                    new ProgressMonitorOutputStream(
                        new FileOutputStream(tempFile),
                        new ProgressMonitorCallback() {
                          @Override
                          public void addToProgress(int value) {
                            addProgress(value);
                          }
                        })) {
              // Not using the IOUtilites copy /copyStream because we
              // want to maximise the efficiency of write(...) methods of
              // the buffered output stream.
              // Redmine #6092
              byte[] bytes = new byte[STREAM_BUF_SIZE];
              int len = 0, accumlen = 0, lastlen = 0;
              while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
                accumlen += len;
                lastlen = len;
              }

              debug(
                  "file is downloaded - "
                      + tempFile.getAbsolutePath()
                      + ", last read "
                      + lastlen
                      + " bytes of "
                      + accumlen
                      + " accumulated bytes");
            } catch (AuthenticationException authException) {
              throw logException("Error (AuthenticationException) downloading file", authException);
            } catch (FileSystemException fileSysException) {
              throw logException("Error (FileSystemException) downloading file", fileSysException);
            } catch (Exception e) {
              throw logException("Error downloading file", e);
            }
            debug("returning tempFile with size: " + tempFile.length());
            return tempFile;
          }

          @Override
          public void finished() {
            file = new CachedFile(get());
            debug("Size of cached file is " + Long.toString(file.length()));
            debug("Starting watch timer with interval " + SYNC_TIME);
            fileWatchTimer = new Timer(true);
            fileWatchTimer.schedule(new FileWatcherTask(), SYNC_TIME, SYNC_TIME);

            // open the file!
            if (openWith) {
              doOpenWith();
            } else {
              doOpen();
            }
          }

          @Override
          protected void afterFinished() {
            // Override to avoid claiming focus
          }
        };
    progWorker.setComponent(this);
    progWorker.start();
  }

  private Pair<InputStream, Integer> readFileFromServer() {
    final Permissions permissions = new Permissions();
    permissions.add(new AllPermission());
    // Sigh... nothing seems to work...
    // permissions.add(new
    // FakeSecureCookiePermission("origin.http://origliasso:80"));
    // permissions.add(new SocketPermission("origliasso:80",
    // "connect,resolve"));

    final AccessControlContext context =
        new AccessControlContext(new ProtectionDomain[] {new ProtectionDomain(null, permissions)});

    return AccessController.doPrivileged(
        new PrivilegedAction<Pair<InputStream, Integer>>() {
          @Override
          public Pair<InputStream, Integer> run() {
            try {
              String url = server.getDownloadUrl(itemUuid, itemVersion, stagingId, filename);
              URLConnection conn = new URL(url).openConnection();
              return new Pair<InputStream, Integer>(conn.getInputStream(), conn.getContentLength());
            } catch (Exception ex) {
              logException("Error downloading from server: " + filename, ex);
              return null;
            }
          }
        },
        context);
  }

  private RuntimeException logException(String msg, Exception ex) {
    logger.log(Level.SEVERE, msg, ex);
    throw new RuntimeException(msg, ex);
  }

  private void initSettings() {
    instanceid = getParameter(INSTANCEID);
    logger.info("initSettings.instanceid is " + instanceid);
    service = getParameter(SERVICE);
    logger.info("initSettings.service is " + service);
    stagingId = getParameter(STAGINGID);
    logger.info("initSettings.stagingId is " + stagingId);
    itemUuid = getParameter(ITEMUUID);
    logger.info("initSettings.itemUuid is " + itemUuid);
    itemVersion = Integer.valueOf(getParameter(ITEMVERSION));
    logger.info("initSettings.itemVersion is " + itemVersion);
    openWith = Boolean.valueOf(getParameter(OPENWITH));
    logger.info("initSettings.openWith is " + openWith);
    filename = getParameter(FILENAME);
    logger.info("initSettings.filename is " + filename);
    mimetype = getParameter(MIMETYPE);
    logger.info("initSettings.mimetype is " + mimetype);
    debug = Boolean.valueOf(getParameter(DEBUG));
    logger.info("initSettings.debug is " + debug);

    String crossDomainXml = getParameter(CROSSDOMAIN);

    String currentVal = System.getProperty("jnlp.altCrossDomainXMLFiles");
    debug("Old cross domain property: " + currentVal);
    String newVal = crossDomainXml;
    if (!Check.isEmpty(currentVal)) {
      newVal = currentVal + "," + crossDomainXml;
    }
    debug("New cross domain property: " + newVal);
    System.setProperty("jnlp.altCrossDomainXMLFiles", newVal);

    try {
      String tempFilename = filename;
      if (filename.contains("/")) {
        final String[] parts = filename.split("/");
        tempFilename = parts[parts.length - 1];
      }

      final File tempFile = File.createTempFile("equella-", '-' + tempFilename);
      filepath = tempFile.getAbsolutePath();
      debug("file path is " + filepath);
    } catch (IOException io) {
      throw logException("Error creating temp file!", io);
    }

    debug("service: " + service);
    debug("stagingId: " + stagingId);
    debug("itemUuid: " + itemUuid);
    debug("itemVersion: " + itemVersion);
    debug("openWith: " + openWith);
    debug("filename: " + filename);
  }

  private void initServer() {
    try {
      server =
          ClientProxyFactory.createProxy(
              InPlaceEditorServerBackend.class, new URL(getEndpoint(), service));
    } catch (MalformedURLException mal) {
      throw logException("Malformed URL: " + getEndpoint().toString() + " + " + service, mal);
    }
  }

  @Override
  protected String[] getBundleGroups() {
    return new String[] {"inplaceeditor-applet"};
  }

  @Override
  protected String getI18nKeyPrefix() {
    return "com.tle.applet.inplaceeditor.";
  }

  /** Used in conjuction with doFromJs */
  @Override
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd != null) {
      if (cmd.equals("open")) {
        doOpen();
      } else if (cmd.equals("openWith")) {
        doOpenWith();
      } else if (cmd.equals("syncFile")) {
        doSyncFile();
      } else if (cmd.equals("hasPendingSync")) {
        doHasPendingSync();
      } else if (cmd.equals("isSynchronising")) {
        doIsSynchronising();
      }
    }
  }

  protected void doFromJs(String cmd) {
    javax.swing.Timer t = new javax.swing.Timer(20, this);
    t.setActionCommand(cmd);
    t.setRepeats(false);
    t.start();
  }

  /** Invoked by Javascript */
  public void syncFile() {
    doFromJs("syncFile");
  }

  private void doSyncFile() {
    try {
      logger.entering("InPlaceEditAppletLauncher", "syncFile");
      if (file == null) {
        logger.severe("File not loaded yet!");
        return;
      }

      debug("Entering synchronized block...");
      synchronized (file) {
        debug("Entered synchronized block - " + file.getAbsolutePath());
        debug(
            "Synchronising: "
                + synchronising
                + ", file.hasChangedSinceLastSync: "
                + file.hasChangedSinceLastSync());

        // Determine if file actually *needs* syncing.
        // This method could be invoked via web or it could be a
        // backlogged TimerTask that is no
        // longer relevant
        if (!synchronising && file.hasChangedSinceLastSync()) {
          final long fileLength = file.length();
          logger.info("File size is " + fileLength);

          final Permissions permissions = new Permissions();
          // permissions.add(new SocketPermission(host, "connect"));
          // debug("Added SocketPermission for 'connect' on " + host);
          // permissions.add(new SocketPermission(host, "resolve"));
          // debug("Added SocketPermission for 'resolve' on " + host);
          // permissions.add(new
          // AWTPermission("showWindowWithoutWarningBanner"));
          // debug("Added AWTPermission for 'showWindowWithoutWarningBanner'");
          // permissions.add(new
          // AWTPermission("listenToAllAWTEvents"));
          // logger.info("Added AWTPermission for 'listenToAllAWTEvents'");
          // permissions.add(new AWTPermission("accessEventQueue"));
          // debug("Added AWTPermission for 'accessEventQueue'");
          permissions.add(new AllPermission());

          final AccessControlContext context =
              new AccessControlContext(
                  new ProtectionDomain[] {new ProtectionDomain(null, permissions)});

          AccessController.doPrivileged(
              new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                  final UploadWorker progWorker =
                      new UploadWorker(
                          CurrentLocale.get("label.uploading"), (int) fileLength, false);
                  progWorker.setComponent(InPlaceEditAppletLauncher.this);
                  progWorker.start();
                  return null;
                }
              },
              context);
        } else {
          debug("Not invoking doSync");
        }
      }
    } catch (Exception e) {
      throw logException("Error invoking syncFile", e);
    }
  }

  /** Invoked by Javascript */
  public void open() {
    doFromJs("open");
  }

  private void doOpen() {
    try {
      logger.entering("InPlaceEditAppletLauncher", "open");

      if (file == null) {
        logger.severe("file not loaded yet!");
        return;
      }

      file.standardOpen();
    } catch (Exception t) {
      logException("Error invoking open", t);
    }
  }

  /** Invoked by Javascript */
  public void openWith() {
    doFromJs("openWith");
  }

  private void doOpenWith() {
    try {
      logger.entering("InPlaceEditAppletLauncher", "openWith");

      if (file == null) {
        logger.severe("file not loaded yet!");
        return;
      }

      opener.openWith(this, filepath, mimetype);
    } catch (Exception t) {
      logException("Error invoking openWith", t);
    }
  }

  /**
   * Invoked by Javascript
   *
   * @return
   */
  public boolean hasPendingSync() {
    doFromJs("hasPendingSync");
    boolean hasPending = false;
    synchronized (resultLock) {
      while (pending == null) {
        try {
          resultLock.wait();
        } catch (InterruptedException e) {
          // Sure
        }
      }
      hasPending = pending;
      pending = null;
    }
    return hasPending;
  }

  private void doHasPendingSync() {
    synchronized (resultLock) {
      try {
        logger.entering("InPlaceEditAppletLauncher", "hasPendingSync");
        if (file != null) {
          synchronized (file) {
            boolean hasPending = file.hasChangedSinceLastSync();
            debug("hasPendingSync() -> " + hasPending);
            pending = hasPending;
          }
          return;
        }

        debug("hasPendingSync() -> false");
        pending = false;
      } catch (Exception e) {
        logException("Error invoking hasPendingSync", e);
      } finally {
        resultLock.notifyAll();
      }
    }
  }

  /**
   * Invoked by Javascript
   *
   * @return
   */
  public boolean isSynchronising() {
    doFromJs("isSynchronising");
    boolean isSynchronising = false;
    synchronized (resultLock) {
      while (syncing == null) {
        try {
          resultLock.wait();
        } catch (InterruptedException e) {
          // Sure
        }
      }
      isSynchronising = syncing;
      syncing = null;
    }
    return isSynchronising;
  }

  private void doIsSynchronising() {
    synchronized (resultLock) {
      try {
        debug("isSynchronising() -> " + synchronising);
        syncing = synchronising;
      } catch (Exception t) {
        syncing = false;
        logException("Error invoking isSynchronising", t);
      } finally {
        resultLock.notifyAll();
      }
    }
  }

  private void debug(String message) {
    if (debug) {
      logger.info(appletId + ": " + message);
    }
  }

  private class CachedFile {
    private final File tempFile;
    private long lastSynced;

    private final AccessControlContext filePermissionContext;
    private final AccessControlContext openPermissionContext;

    protected String getAbsolutePath() {
      return tempFile != null ? tempFile.getAbsolutePath() : null;
    }

    public CachedFile(File tempFile) {
      this.tempFile = tempFile;

      final Permissions filePermissions = new Permissions();
      final FilePermission crudPermission =
          new FilePermission(tempFile.getAbsolutePath(), "read,write,delete");
      filePermissions.add(crudPermission);
      debug(
          "filePermissions Added FilePermission for 'read', 'write', 'delete' on "
              + tempFile.getAbsolutePath());
      filePermissionContext =
          new AccessControlContext(
              new ProtectionDomain[] {new ProtectionDomain(null, filePermissions)});

      final Permissions openPermissions = new Permissions();
      openPermissions.add(crudPermission);
      debug(
          "openPermissions Added FilePermission for 'read', 'write', 'delete' on "
              + tempFile.getAbsolutePath());
      openPermissions.add(new FilePermission("<<ALL FILES>>", "execute"));
      debug("openPermissions Added FilePermission for 'execute' on <<ALL FILES>>");
      openPermissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
      debug("openPermissions Added AWTPermission for 'showWindowWithoutWarningBanner'");
      openPermissionContext =
          new AccessControlContext(
              new ProtectionDomain[] {new ProtectionDomain(null, openPermissions)});

      setAsSynced();
    }

    public void standardOpen() {
      AccessController.doPrivileged(
          new PrivilegedAction<Object>() {
            @Override
            public Object run() {
              try {
                debug("using Desktop.open");
                Desktop.getDesktop().open(tempFile);
              } catch (Exception io) {
                logException("Error opening file", io);
              }
              return null;
            }
          },
          openPermissionContext);
    }

    public boolean hasChangedSinceLastSync() {
      return lastSynced != getLastModified();
    }

    public final void setAsSynced() {
      lastSynced = getLastModified();
    }

    private long getLastModified() {
      return AccessController.doPrivileged(
          new PrivilegedAction<Long>() {
            @Override
            public Long run() {
              return tempFile.lastModified();
            }
          },
          filePermissionContext);
    }

    public long length() {
      return AccessController.doPrivileged(
          new PrivilegedAction<Long>() {
            @Override
            public Long run() {
              return tempFile.length();
            }
          },
          filePermissionContext);
    }

    public InputStream read() {
      return AccessController.doPrivileged(
          new PrivilegedAction<InputStream>() {
            @Override
            public InputStream run() {
              try {
                return new FileInputStream(tempFile);
              } catch (FileNotFoundException fnf) {
                throw logException("Error reading file", fnf);
              }
            }
          },
          filePermissionContext);
    }
  }

  protected class FileWatcherTask extends TimerTask {
    @Override
    public void run() {
      logger.entering("InPlaceEditAppletLauncher.FileWatcherTask", "run");
      if (file != null && file.hasChangedSinceLastSync()) {
        saveButton.setVisible(true);
        ignoreChangesButton.setVisible(true);
      } else {
        saveButton.setVisible(false);
        ignoreChangesButton.setVisible(false);
      }
    }
  }

  private class UploadWorker extends GlassProgressWorker<Object> {
    public UploadWorker(String message, int maxProgress, boolean cancellable) {
      super(message, maxProgress, cancellable);
      synchronising = true;
    }

    @Override
    public Object construct() throws Exception {
      debug("Reading file from disk");
      try (InputStream in =
          new BufferedInputStream(
              new ProgressMonitorInputStream(
                  file.read(),
                  new ProgressMonitorCallback() {
                    @Override
                    public void addToProgress(int value) {
                      addProgress(value);
                    }
                  }))) {
        logger.info("Sending to server");
        byte[] bytes = new byte[STREAM_BUF_SIZE];
        int len;
        boolean append = false;
        while ((len = in.read(bytes)) != -1) {
          byte[] b2 = bytes;
          if (len != STREAM_BUF_SIZE) {
            b2 = new byte[len];
            System.arraycopy(bytes, 0, b2, 0, len);
          }
          debug("Writing " + b2.length + " bytes");
          server.write(stagingId, filename, append, b2);
          append = true;
        }
        logger.info("Upload complete");

        debug("Updating synchronised status");
        file.setAsSynced();
        return null;
      } catch (Exception t) {
        logException("Error syncing file", t);
        return null;
      }
    }

    @Override
    public void finished() {
      super.finished();
      synchronising = false;
      saveButton.setVisible(false);
      ignoreChangesButton.setVisible(false);
    }

    @Override
    protected void afterFinished() {
      // Nothing. Stop asking for focus!
    }
  }

  private class SaveActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      doSyncFile();
    }
  }

  private class CancelActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      file.setAsSynced();
    }
  }
}
