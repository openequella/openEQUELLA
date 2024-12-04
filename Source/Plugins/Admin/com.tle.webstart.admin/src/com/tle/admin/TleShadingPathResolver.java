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

package com.tle.admin;

import com.dytech.gui.ComponentHelper;
import com.tle.common.applet.gui.AppletGuiUtils;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.UniqueIdentity;
import org.java.plugin.standard.StandardPathResolver;
import org.java.plugin.util.ExtendedProperties;
import org.java.plugin.util.IoUtil;

/**
 * This is a copy of the ShadingPathResolver distributed with JPF, but with specific modifications
 * to ensure it works in the Administration Console environment.
 */
@SuppressWarnings("nls")
public class TleShadingPathResolver extends StandardPathResolver {
  private static final String UNPACK_MODE_ALWAIS = "always";
  private static final String UNPACK_MODE_NEVER = "never";
  private static final String UNPACK_MODE_SMART = "smart";

  private File shadowFolder;
  private String unpackMode;
  private final Map<String, URL> shadowUrlMap = new HashMap<String, URL>();
  private final Map<String, Boolean> unpackModeMap = new HashMap<String, Boolean>();
  private ShadowDataController controller;
  private ProgressListener listener;

  @Override
  public synchronized void configure(final ExtendedProperties config) throws Exception {
    super.configure(config);
    String folder = config.getProperty("shadowFolder");
    if ((folder != null) && (folder.length() > 0)) {
      try {
        shadowFolder = new File(folder).getCanonicalFile();
      } catch (IOException ioe) {
        log.warn(
            "failed initializing shadow folder " + folder + ", falling back to the default folder",
            ioe);
      }
    }
    if (shadowFolder == null) {
      shadowFolder = new File(System.getProperty("java.io.tmpdir"), ".jpf-shadow");
    }
    log.debug("shadow folder is " + shadowFolder);
    if (!shadowFolder.exists()) {
      shadowFolder.mkdirs();
    }
    unpackMode = config.getProperty("unpackMode", UNPACK_MODE_SMART);
    log.debug("unpack mode parameter value is " + unpackMode);
    controller = ShadowDataController.init(shadowFolder, buildFileFilter(config));
    log.info("configured, shadow folder is " + shadowFolder);

    listener =
        new ProgressListener() {
          private JDialog dialog;
          private int dialogRefCount;
          private final JProgressBar progress = new JProgressBar();
          private final Timer closeTimer = new Timer();

          @Override
          public void start() {
            SwingUtilities.invokeLater(
                new Runnable() {
                  @Override
                  public void run() {
                    initDialog();
                    progress.setIndeterminate(true);
                  }
                });
          }

          @Override
          public void total(final int total) {
            SwingUtilities.invokeLater(
                new Runnable() {
                  @Override
                  public void run() {
                    boolean indeterminate = total < 0;
                    progress.setIndeterminate(indeterminate);
                    if (!indeterminate) {
                      progress.setMaximum(total);
                      progress.setValue(0);
                    }
                  }
                });
          }

          @Override
          public void add(final int amount) {
            SwingUtilities.invokeLater(
                new Runnable() {
                  @Override
                  public void run() {
                    progress.setValue(progress.getValue() + amount);
                  }
                });
          }

          @Override
          public void complete() {
            closeTimer.schedule(
                new TimerTask() {
                  @Override
                  public void run() {
                    SwingUtilities.invokeLater(
                        new Runnable() {
                          @Override
                          public void run() {
                            closeDialog();
                          }
                        });
                  }
                },
                500);
          }

          private void initDialog() {
            if (dialog == null) {
              JLabel label = new JLabel("Downloading...");
              label.setHorizontalAlignment(SwingConstants.CENTER);
              label.setHorizontalTextPosition(SwingConstants.CENTER);

              JPanel panel = new JPanel(new BorderLayout(5, 5));
              panel.setBorder(
                  BorderFactory.createCompoundBorder(
                      BorderFactory.createRaisedBevelBorder(), AppletGuiUtils.DEFAULT_BORDER));
              panel.add(label, BorderLayout.NORTH);
              panel.add(progress, BorderLayout.SOUTH);

              Frame[] frames = Frame.getFrames();
              dialog = frames.length > 0 ? new JDialog(frames[frames.length - 1]) : new JDialog();

              dialog.setUndecorated(true);
              dialog.setAlwaysOnTop(true);
              dialog.getContentPane().add(panel);
              dialog.pack();
              ComponentHelper.centreOnScreen(dialog);
              dialog.setVisible(true);
            }
            dialogRefCount++;
          }

          private void closeDialog() {
            dialogRefCount--;
            if (dialogRefCount <= 0 && dialog != null) {
              dialog.setVisible(false);
              dialog.dispose();
              dialog = null;
            }
          }
        };
  }

  private FileFilter buildFileFilter(final ExtendedProperties config) {
    final FileFilter includesFilter;
    String patterns = config.getProperty("includes");
    if ((patterns != null) && (patterns.trim().length() > 0)) {
      includesFilter = new RegexpFileFilter(patterns);
    } else {
      includesFilter = null;
    }
    final FileFilter excludesFilter;
    patterns = config.getProperty("excludes");
    if ((patterns != null) && (patterns.trim().length() > 0)) {
      excludesFilter = new RegexpFileFilter(patterns);
    } else {
      excludesFilter = null;
    }
    if ((excludesFilter == null) && (includesFilter == null)) {
      return null;
    }
    return new CombinedFileFilter(includesFilter, excludesFilter);
  }

  @Override
  public synchronized void registerContext(Identity idt, URL url) {
    super.registerContext(idt, url);
    Boolean mode;
    if (UNPACK_MODE_ALWAIS.equalsIgnoreCase(unpackMode)) {
      mode = Boolean.TRUE;
    } else if (UNPACK_MODE_NEVER.equalsIgnoreCase(unpackMode)) {
      mode = Boolean.FALSE;
    } else {
      PluginDescriptor descr = null;
      PluginFragment fragment = null;
      if (idt instanceof PluginDescriptor) {
        descr = (PluginDescriptor) idt;
      } else if (idt instanceof PluginFragment) {
        fragment = (PluginFragment) idt;
        descr = fragment.getRegistry().getPluginDescriptor(fragment.getPluginId());
      } else if (idt instanceof PluginElement<?>) {
        PluginElement<?> element = (PluginElement<?>) idt;
        descr = element.getDeclaringPluginDescriptor();
        fragment = element.getDeclaringPluginFragment();
      } else {
        throw new IllegalArgumentException("unknown identity class " + idt.getClass().getName());
      }
      mode = getUnpackMode(descr, fragment);
    }
    log.debug("unpack mode for " + idt + " is " + mode);
    unpackModeMap.put(idt.getId(), mode);
  }

  private Boolean getUnpackMode(final PluginDescriptor descr, final PluginFragment fragment) {
    for (PluginAttribute attr : filterCollection(descr.getAttributes("unpack"), fragment)) {
      return Boolean.valueOf(attr.getValue());
    }

    for (Library lib : filterCollection(descr.getLibraries(), fragment)) {
      if (lib.isCodeLibrary()
          && (lib.getPath().toLowerCase(Locale.getDefault()).endsWith(".jar")
              || lib.getPath().toLowerCase(Locale.getDefault()).endsWith(".zip"))) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  private <T extends PluginElement<?>> Collection<T> filterCollection(
      final Collection<T> coll, final PluginFragment fragment) {
    if (fragment == null) {
      return coll;
    }
    LinkedList<T> result = new LinkedList<T>();
    for (T element : coll) {
      if (fragment.equals(element.getDeclaringPluginFragment())) {
        result.add(element);
      }
    }
    return result;
  }

  @Override
  public void unregisterContext(String id) {
    shadowUrlMap.remove(id);
    unpackModeMap.remove(id);
    super.unregisterContext(id);
  }

  @Override
  public URL resolvePath(final Identity idt, final String path) {
    URL baseUrl;
    if (idt instanceof PluginDescriptor) {
      baseUrl = getBaseUrl((PluginDescriptor) idt);
    } else if (idt instanceof PluginFragment) {
      baseUrl = getBaseUrl((PluginFragment) idt);
    } else if (idt instanceof PluginElement) {
      PluginElement<?> element = (PluginElement<?>) idt;
      if (element.getDeclaringPluginFragment() != null) {
        baseUrl = getBaseUrl(element.getDeclaringPluginFragment());
      } else {
        baseUrl = getBaseUrl(element.getDeclaringPluginDescriptor());
      }
    } else {
      throw new IllegalArgumentException("unknown identity class " + idt.getClass().getName());
    }
    return resolvePath(baseUrl, path);
  }

  protected synchronized URL getBaseUrl(final UniqueIdentity uid) {
    URL result = shadowUrlMap.get(uid.getId());
    if (result != null) {
      return result;
    }
    try {
      listener.start();
      result =
          controller.shadowResource(
              getRegisteredContext(uid.getId()),
              uid.getUniqueId().replace(':', '_'),
              (unpackModeMap.get(uid.getId())).booleanValue(),
              listener);
      shadowUrlMap.put(uid.getId(), result);
    } finally {
      listener.complete();
    }
    return result;
  }
}

@SuppressWarnings("nls")
final class ShadingUtil {
  private static final String JAR = "jar";
  private static final String FOLDER_ERROR = "can't create folder ";

  static String getExtension(final String name) {
    if ((name == null) || (name.length() == 0)) {
      return null;
    }
    int p = name.lastIndexOf('.');
    if ((p != -1) && (p > 0) && (p < name.length() - 1)) {
      return name.substring(p + 1);
    }
    return null;
  }

  static void unpack(final ZipFile zipFile, final File destFolder) throws IOException {
    zipFile.size();
    for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements(); ) {
      ZipEntry entry = en.nextElement();
      String name = entry.getName();
      File entryFile = new File(destFolder.getCanonicalPath() + "/" + name);
      if (name.endsWith("/")) {
        if (!entryFile.exists() && !entryFile.mkdirs()) {
          throw new IOException(FOLDER_ERROR + entryFile);
        }
      } else {
        File folder = entryFile.getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
          throw new IOException(FOLDER_ERROR + folder);
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(entryFile, false));
        try {
          InputStream in = zipFile.getInputStream(entry);
          try {
            IoUtil.copyStream(in, out, 1024);
          } finally {
            in.close();
          }
        } finally {
          out.close();
        }
      }
      entryFile.setLastModified(entry.getTime());
    }
  }

  static void unpack(final InputStream strm, final File destFolder) throws IOException {
    ZipInputStream zipStrm = new ZipInputStream(strm);
    ZipEntry entry = zipStrm.getNextEntry();
    while (entry != null) {
      String name = entry.getName();
      File entryFile = new File(destFolder.getCanonicalPath() + "/" + name);
      if (name.endsWith("/")) {
        if (!entryFile.exists() && !entryFile.mkdirs()) {
          throw new IOException(FOLDER_ERROR + entryFile);
        }
      } else {
        File folder = entryFile.getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
          throw new IOException(FOLDER_ERROR + folder);
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(entryFile, false));
        try {
          IoUtil.copyStream(zipStrm, out, 1024);
        } finally {
          out.close();
        }
      }
      entryFile.setLastModified(entry.getTime());
      entry = zipStrm.getNextEntry();
    }
  }

  static boolean deleteFile(final File file) {
    if (file.isDirectory()) {
      IoUtil.emptyFolder(file);
    }
    return file.delete();
  }

  static Date getLastModified(final URL url) throws IOException {
    long result = 0;
    if (JAR.equalsIgnoreCase(url.getProtocol())) {
      String urlStr = url.toExternalForm();
      int p = urlStr.indexOf("!/");
      if (p != -1) {
        return getLastModified(new URL(urlStr.substring(4, p)));
      }
    }
    File sourceFile = IoUtil.url2file(url);
    if (sourceFile != null) {
      result = sourceFile.lastModified();
    } else {
      URLConnection cnn = url.openConnection();
      try {
        cnn.setUseCaches(false);
        // this should force using HTTP HEAD method
        if (cnn instanceof HttpURLConnection) {
          ((HttpURLConnection) cnn).setRequestMethod("HEAD");
        }
        result = cnn.getLastModified();
      } finally {
        try {
          cnn.getInputStream().close();
        } catch (Exception ioe) {
          // ignore
        }
      }
    }
    if (result == 0) {
      throw new IOException("can't retrieve modification date for resource " + url);
    }
    // for some reason modification milliseconds for some files are unstable
    Calendar cldr = Calendar.getInstance(Locale.ENGLISH);
    cldr.setTime(new Date(result));
    cldr.set(Calendar.MILLISECOND, 0);
    return cldr.getTime();
  }

  private static String getRelativePath(final File base, final File file) throws IOException {
    String basePath;
    String filePath = file.getCanonicalPath();
    if (base.isFile()) {
      File baseParent = base.getParentFile();
      if (baseParent == null) {
        return null;
      }
      basePath = baseParent.getCanonicalPath();
    } else {
      basePath = base.getCanonicalPath();
    }
    if (!basePath.endsWith(File.separator)) {
      basePath += File.separator;
    }
    int p = basePath.indexOf(File.separatorChar);
    String prefix = null;
    while (p != -1) {
      String newPrefix = basePath.substring(0, p + 1);
      if (!filePath.startsWith(newPrefix)) {
        break;
      }
      prefix = newPrefix;
      p = basePath.indexOf(File.separatorChar, p + 1);
    }
    if (prefix == null) {
      return null;
    }
    filePath = filePath.substring(prefix.length());
    if (prefix.length() == basePath.length()) {
      return filePath;
    }
    int c = 0;
    p = basePath.indexOf(File.separatorChar, prefix.length());
    while (p != -1) {
      c++;
      p = basePath.indexOf(File.separatorChar, p + 1);
    }
    for (int i = 0; i < c; i++) {
      filePath = ".." + File.separator + filePath;
    }
    return filePath;
  }

  private static String getRelativeUrl(final File base, final File file) throws IOException {
    String result = getRelativePath(base, file);
    if (result == null) {
      return null;
    }
    result = result.replace('\\', '/');
    if (file.isDirectory() && !result.endsWith("/")) {
      result += "/";
    }
    return result;
  }

  static String getRelativeUrl(final File base, final URL url) throws IOException {
    File file = IoUtil.url2file(url);
    if (file != null) {
      String result = getRelativeUrl(base, file);
      if (result != null) {
        String urlStr = url.toExternalForm();
        int p = urlStr.indexOf("!/");
        if (p != -1) {
          return "jar:" + result + urlStr.substring(p);
        }
        return result;
      }
    }

    if (JAR.equalsIgnoreCase(url.getProtocol())) {
      String urlStr = url.toExternalForm();
      int p = urlStr.indexOf("!/");
      if (p != -1) {
        return "jar:" + getRelativeUrl(base, new URL(urlStr.substring(4, p))) + urlStr.substring(p);
      }
    }
    return url.toExternalForm();
  }

  static URL buildURL(final URL base, final String url) throws MalformedURLException {
    if (!url.toLowerCase(Locale.ENGLISH).startsWith("jar:")) {
      return new URL(base, url);
    }
    int p = url.indexOf("!/");
    if (p == -1) {
      return new URL(base, url.substring(4, p));
    }
    return new URL("jar:" + new URL(base, url.substring(4, p)).toExternalForm() + url.substring(p));
  }

  private ShadingUtil() {
    // no-op
  }
}

@SuppressWarnings("nls")
final class ShadowDataController {
  private static final String MODIFIED = "modified:";
  private static final String FILE = "file:";
  private static final String SOURCE = "source:";

  private static final String META_FILE_NAME = ".meta";

  private final Log log = LogFactory.getLog(ShadowDataController.class);
  private final File shadowFolder;
  private final URL shadowFolderUrl;
  private final Properties metaData;
  private final DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final FileFilter fileFilter;

  static ShadowDataController init(final File shadowFolder, final FileFilter filter)
      throws IOException {
    ShadowDataController result = new ShadowDataController(shadowFolder, filter);
    result.quickCheck();
    result.save();
    return result;
  }

  private ShadowDataController(final File folder, final FileFilter filter) throws IOException {
    shadowFolder = folder;
    fileFilter = filter;
    shadowFolderUrl = IoUtil.file2url(folder);
    File metaFile = new File(shadowFolder, META_FILE_NAME);
    metaData = new Properties();
    if (metaFile.isFile()) {
      try {
        InputStream in = new FileInputStream(metaFile);
        try {
          metaData.load(in);
        } finally {
          in.close();
        }
        if (log.isDebugEnabled()) {
          log.debug("meta-data loaded from file " + metaFile);
        }
      } catch (IOException ioe) {
        log.warn("failed loading meta-data from file " + metaFile, ioe);
      }
    }
  }

  private void save() {
    File metaFile = new File(shadowFolder, META_FILE_NAME);
    try {
      OutputStream out = new FileOutputStream(metaFile, false);
      try {
        metaData.store(out, "This is automatically generated file.");
      } finally {
        out.close();
      }
      if (log.isDebugEnabled()) {
        log.debug("meta-data saved to file " + metaFile);
      }
    } catch (IOException ioe) {
      log.warn("failed saving meta-data to file " + metaFile, ioe);
    }
  }

  private void quickCheck() {
    File[] files = shadowFolder.listFiles(new ShadowFileFilter());
    for (File file : files) {
      if (metaData.containsValue(file.getName())) {
        continue;
      }
      if (ShadingUtil.deleteFile(file)) {
        if (log.isDebugEnabled()) {
          log.debug("deleted shadow file " + file);
        }
      } else {
        log.warn("can't delete shadow file " + file);
      }
    }
    Set<Object> uids = new HashSet<Object>();
    for (Map.Entry<Object, Object> entry : metaData.entrySet()) {
      String key = (String) entry.getKey();
      if (!key.startsWith("uid:")) {
        continue;
      }
      uids.add(entry.getValue());
    }
    for (Object object : uids) {
      quickCheck((String) object);
    }
  }

  private void quickCheck(final String uid) {
    if (log.isDebugEnabled()) {
      log.debug("quick check of UID " + uid);
    }
    String url = metaData.getProperty(SOURCE + uid, null);
    String file = metaData.getProperty(FILE + uid, null);
    String modified = metaData.getProperty(MODIFIED + uid, null);
    if ((url == null) || (file == null) || (modified == null)) {
      if (log.isDebugEnabled()) {
        log.debug("meta-data incomplete, UID=" + uid);
      }
      remove(uid);
      return;
    }
    try {
      if (!dtf.parse(modified)
          .equals(ShadingUtil.getLastModified(ShadingUtil.buildURL(shadowFolderUrl, url)))) {
        if (log.isDebugEnabled()) {
          log.debug("source modification detected, UID=" + uid + ", source=" + url);
        }
        remove(uid);
      }
    } catch (IOException ioe) {
      // log.warn("quick check failed", ioe);
      remove(uid);
    } catch (ParseException pe) {
      // log.warn("quick check failed", pe);
      remove(uid);
    }
  }

  private void remove(final String uid) {
    String file = metaData.getProperty(FILE + uid, null);
    if (file != null) {
      File lostFile = new File(shadowFolder, file);
      if (ShadingUtil.deleteFile(lostFile)) {
        if (log.isDebugEnabled()) {
          log.debug("deleted lost file " + file);
        }
      } else {
        log.warn("can't delete lost file " + file);
      }
    }
    boolean removed = metaData.remove("uid:" + uid) != null;
    removed |= metaData.remove(SOURCE + uid) != null;
    removed |= metaData.remove(FILE + uid) != null;
    removed |= metaData.remove(MODIFIED + uid) != null;
    if (removed && log.isDebugEnabled()) {
      log.debug("removed meta-data, UID=" + uid);
    }
  }

  private URL add(final String uid, final URL sourceUrl, final File file, final Date modified)
      throws IOException {
    URL result = IoUtil.file2url(file);
    metaData.setProperty("uid:" + uid, uid);
    String source = ShadingUtil.getRelativeUrl(shadowFolder, sourceUrl);
    if (log.isDebugEnabled()) {
      log.debug("relativize: base=" + shadowFolder + "; url=" + sourceUrl + "; result=" + source);
    }

    metaData.setProperty(SOURCE + uid, source);
    metaData.setProperty(FILE + uid, file.getName());
    metaData.setProperty(MODIFIED + uid, dtf.format(modified));
    save();
    if (log.isDebugEnabled()) {
      log.debug(
          "shading done, UID="
              + uid
              + ", source="
              + source
              + ", file="
              + result
              + ", modified="
              + dtf.format(modified));
    }
    return result;
  }

  URL shadowResource(
      final URL source, final String uid, final boolean unpack, ProgressListener listener) {
    try {
      URL result = deepCheck(source, uid);
      if (result != null) {
        if (log.isDebugEnabled()) {
          log.debug(
              "got actual shaded resource, UID=" + uid + ", source=" + source + ", file=" + result);
        }
        return result;
      }
    } catch (Exception e) {
      log.warn("deep check failed, UID=" + uid + ", URL=" + source, e);
      remove(uid);
    }
    Date lastModified;
    try {
      lastModified = ShadingUtil.getLastModified(source);
    } catch (IOException ioe) {
      log.error("shading failed, can't get modification date for " + source, ioe);
      return source;
    }

    File file = IoUtil.url2file(source);
    if ((file != null) && file.isDirectory()) {
      // copy local folder to the shadow directory
      try {
        File rootFolder = new File(shadowFolder, uid);
        IoUtil.copyFolder(file, rootFolder, true, true, fileFilter);
        return add(uid, source, rootFolder, lastModified);
      } catch (IOException ioe) {
        log.error("failed shading local folder " + file, ioe);
        return source;
      }
    }

    try {
      if ("jar".equalsIgnoreCase(source.getProtocol())) {
        String urlStr = source.toExternalForm();
        int p = urlStr.indexOf("!/");
        if (p == -1) {
          p = urlStr.length();
        }
        URL jarFileURL = new URL(urlStr.substring(4, p));

        if (!unpack) {
          String ext = ShadingUtil.getExtension(jarFileURL.getFile());
          if (ext == null) {
            ext = "jar";
          }
          File shadowFile = new File(shadowFolder, uid + '.' + ext);
          File sourceFile = IoUtil.url2file(jarFileURL);
          InputStream in;
          if (sourceFile != null) {
            in = new FileInputStream(sourceFile);
          } else {
            in = jarFileURL.openStream();
          }
          try {
            OutputStream out = new FileOutputStream(shadowFile, false);
            try {
              IoUtil.copyStream(in, out, 1024);
            } finally {
              out.close();
            }
          } finally {
            in.close();
          }
          return add(uid, source, shadowFile, lastModified);
        }

        File sourceFile = IoUtil.url2file(jarFileURL);
        ZipFile zipFile;
        if (sourceFile != null) {
          zipFile = new ZipFile(sourceFile);
        } else {
          // We can't explicitly close the following connection
          // because we can't call getInputStream(), and this requires
          // that our URL points to an entry in the JAR, not just the
          // JAR itself. Let's hope the GC does it's work :)
          URLConnection cnn = source.openConnection();
          cnn.setUseCaches(false);
          zipFile = ((JarURLConnection) cnn).getJarFile();
        }

        File rootFolder = new File(shadowFolder, uid);
        try {
          ShadingUtil.unpack(zipFile, rootFolder);
        } finally {
          zipFile.close();
        }
        return add(uid, source, rootFolder, lastModified);
      }
    } catch (IOException ioe) {
      log.error("failed shading URL connection " + source, ioe);
    }
    return source;
  }

  public static void copyStream(
      final InputStream in,
      final OutputStream out,
      final int bufferSize,
      final ProgressListener listener)
      throws IOException {
    byte[] buf = new byte[bufferSize];
    int len;
    while ((len = in.read(buf)) != -1) {
      out.write(buf, 0, len);
      if (listener != null) {
        listener.add(len);
      }
    }
  }

  private URL deepCheck(final URL source, final String uid) throws Exception {
    String url = metaData.getProperty(SOURCE + uid, null);
    if (url == null) {
      if (log.isDebugEnabled()) {
        log.debug("URL not found in meta-data, UID=" + uid);
      }
      remove(uid);
      return null;
    }
    if (log.isDebugEnabled()) {
      log.debug("URL found in meta-data, UID=" + uid + ", source=" + source + ", storedURL=" + url);
    }
    URL storedSource = ShadingUtil.buildURL(shadowFolderUrl, url);
    // Per Sonar, comparing URLs directly causes lookup of the hosts, which we don't want.
    if (!storedSource.toString().equals(source.toString())) {
      if (log.isDebugEnabled()) {
        log.debug(
            "inconsistent URL found in meta-data, UID="
                + uid
                + ", source="
                + source
                + ", storedSource="
                + storedSource);
      }
      remove(uid);
      return null;
    }
    String modified = metaData.getProperty(MODIFIED + uid, null);
    if (modified == null) {
      if (log.isDebugEnabled()) {
        log.debug("modification info not found in meta-data, UID=" + uid);
      }
      remove(uid);
      return null;
    }
    if (!ShadingUtil.getLastModified(source).equals(dtf.parse(modified))) {
      if (log.isDebugEnabled()) {
        log.debug("source modification detected, UID=" + uid + ", source=" + source);
      }
      remove(uid);
      return null;
    }
    String fileStr = metaData.getProperty(FILE + uid, null);
    if (fileStr == null) {
      if (log.isDebugEnabled()) {
        log.debug("file info not found in meta-data, UID=" + uid);
      }
      remove(uid);
      return null;
    }
    File file = new File(shadowFolder, fileStr);
    if (!file.exists()) {
      if (log.isDebugEnabled()) {
        log.debug("shadow file not found, UID=" + uid + ", source=" + source + ", file=" + file);
      }
      remove(uid);
      return null;
    }
    File sourceFile = IoUtil.url2file(source);
    if ((sourceFile != null) && sourceFile.isDirectory()) {
      IoUtil.synchronizeFolders(sourceFile, file, fileFilter);
      if (log.isDebugEnabled()) {
        log.debug(
            "folders synchronized, UID=" + uid + ", srcFile=" + sourceFile + ", destFile=" + file);
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(
            "source "
                + source
                + " (file is "
                + sourceFile
                + ") is not local folder, "
                + "skipping synchronization, UID="
                + uid);
      }
    }
    return IoUtil.file2url(file);
  }

  static class ShadowFileFilter implements FileFilter {
    /**
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(final File file) {
      return !META_FILE_NAME.equals(file.getName());
    }
  }
}

@SuppressWarnings("nls")
final class RegexpFileFilter implements FileFilter {
  private final Pattern[] patterns;

  RegexpFileFilter(final String str) {
    StringTokenizer st = new StringTokenizer(str, "|", false);
    patterns = new Pattern[st.countTokens()];
    for (int i = 0; i < patterns.length; i++) {
      String pattern = st.nextToken();
      if ((pattern == null) || (pattern.trim().length() == 0)) {
        continue;
      }
      patterns[i] = Pattern.compile(pattern.trim());
    }
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(final File file) {
    for (Pattern element : patterns) {
      if (element == null) {
        continue;
      }
      if (element.matcher(file.getName()).matches()) {
        return true;
      }
    }
    return false;
  }
}

final class CombinedFileFilter implements FileFilter {
  private final FileFilter includesFilter;
  private final FileFilter excludesFilter;

  CombinedFileFilter(final FileFilter includes, final FileFilter excludes) {
    includesFilter = includes;
    excludesFilter = excludes;
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(final File file) {
    if (includesFilter != null) {
      if (includesFilter.accept(file)) {
        return true;
      }
    }
    if ((excludesFilter != null) && excludesFilter.accept(file)) {
      return false;
    }
    return true;
  }
}

interface ProgressListener {
  void start();

  void total(int i);

  void add(int i);

  void complete();
}
