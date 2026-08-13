package com.tle.webtests.framework;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class TestConfig {
  private static final String INSTITUTION_PROPS = "institution.properties";

  /** Directory under the base folder holding the per-institution fixture trees. */
  private static final String INSTITUTIONS_DIR = "institutions";

  /** The other marker identifying the base folder, alongside {@link #INSTITUTIONS_DIR}. */
  private static final String BUILD_DEFINITION = "build.sbt";

  /** Config property overriding base folder discovery, relative to the working directory. */
  private static final String BASE_FOLDER_PROPERTY = "test.base";

  private static final Config config = ConfigFactory.load();
  private static File baseFolder = null;

  private final boolean alertSupported;
  private final boolean noInstitution;
  private final File testFolder;
  private TimeZone _browserTimeZone;

  public TestConfig(Class<?> clazz) {
    this(clazz, false);
  }

  public TestConfig(Class<?> clazz, boolean noInstitution) {
    this(noInstitution ? getBaseFolder() : findInstitutionFolder(clazz), noInstitution);
  }

  public TestConfig(File testFolder, boolean noInstitution) {
    this.noInstitution = noInstitution;
    alertSupported = Boolean.parseBoolean(getProperty("webdriver.alerts", "false"));
    this.testFolder = testFolder;
  }

  public boolean isNoInstitution() {
    return noInstitution;
  }

  /**
   * Finds the folder above the "classes" folder for this class
   *
   * @return
   */
  private static File findInstitutionFolder(Class<?> clazz) {
    String folderName = findInstitutionName(clazz);
    return findInstitutionFolder(folderName);
  }

  /**
   * The directory holding every per-institution fixture tree.
   *
   * <p>Verified to exist, because callers reach straight for {@link File#listFiles()} - which
   * returns null for a missing directory, and so fails later with an opaque NullPointerException
   * rather than naming the path that was wrong.
   */
  public static File getInstitutionsFolder() {
    File institutions = new File(getBaseFolder(), INSTITUTIONS_DIR);
    if (!institutions.isDirectory()) {
      throw new IllegalStateException("No institution fixtures at " + institutions);
    }
    return institutions;
  }

  private static File findInstitutionFolder(String name) {
    return new File(getInstitutionsFolder(), name);
  }

  public static String findInstitutionName(Class<?> clazz) {
    String inst;
    TestInstitution annotation = clazz.getAnnotation(TestInstitution.class);
    if (annotation == null) {
      throw new Error("Tests must be annotated with @TestInstitution now");
    } else {
      inst = annotation.value();
    }

    return inst;
  }

  public Duration getStandardTimeout() {
    return Duration.ofSeconds(getIntProperty("timeout.standard", 30));
  }

  public String getAdminPassword() {
    return getProperty("server.password");
  }

  public String getAdminUrl() {
    String adminUrl = getProperty("admin.url");
    if (adminUrl == null) {
      return getServerUrl();
    }
    return adminUrl;
  }

  public String getServerUrl() {
    return getServerUrl(isSsl());
  }

  public String getServerUrl(boolean ssl) {
    String serverUrl = getProperty("server.url");
    if (!serverUrl.endsWith("/")) {
      serverUrl += '/';
    }
    if (ssl) {
      try {
        URL url = new URL(serverUrl);
        int sslPort = getSslPort();
        if (sslPort == 443) {
          serverUrl = new URL("https", url.getHost(), url.getFile()).toExternalForm();
        } else {
          serverUrl = new URL("https", url.getHost(), sslPort, url.getFile()).toExternalForm();
        }
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return serverUrl;
  }

  public boolean isSsl() {
    return isSsl(getTestFolder());
  }

  public boolean isSsl(File testFolder) {
    boolean https = testFolder.getName().endsWith("ssl");
    if (!https) {
      try {
        Properties props = getInstProperties(testFolder);
        if (props != null) {
          https = Boolean.parseBoolean(props.getProperty("https", "false"));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return https;
  }

  public int getSslPort() {
    String sslPort = getProperty("server.ssl.port");
    if (StringUtils.isBlank(sslPort)) {
      return 8443;
    }
    return Integer.parseInt(sslPort);
  }

  public String getMoodleUrl(String version) {
    String serverUrl = getProperty("moodle." + version + ".url");
    if (serverUrl != null && !serverUrl.endsWith("/")) {
      serverUrl += '/';
    }
    return serverUrl;
  }

  public String getMoodleContextUrl(String version) {
    String moodleUrl = getMoodleUrl(version);
    return StringUtils.isBlank(moodleUrl)
        ? moodleUrl
        : MessageFormat.format("{0}moodle{1}/", moodleUrl, version);
  }

  public String getIntegrationUrl(String integ) {
    String serverUrl = getProperty(integ + ".url");
    if (serverUrl != null && !serverUrl.endsWith("/")) {
      serverUrl += '/';
    }
    return serverUrl;
  }

  public String getProperty(String property) {
    return getProperty(property, null);
  }

  public String getProperty(String property, String defaultValue) {
    if (config.hasPath(property)) {
      return config.getString(property);
    } else {
      return defaultValue;
    }
  }

  public int getIntProperty(String property, int defaultValue) {
    String val = getProperty(property);
    if (StringUtils.isNotBlank(val)) {
      try {
        return Integer.parseInt(val);
      } catch (NumberFormatException ex) {
        // Fall through
      }
    }
    return defaultValue;
  }

  public boolean isNewUI() {
    return getBooleanProperty("tests.newui", false);
  }

  public boolean getBooleanProperty(String property, boolean defaultValue) {
    String val = getProperty(property);
    if (StringUtils.isNotBlank(val)) {
      return Boolean.parseBoolean(val);
    }
    return defaultValue;
  }

  public File getScreenshotFolder() {
    return new File(getResultsFolder(), "screenshots");
  }

  public File getResultsFolder() {
    return new File(getBaseFolder(), "target/test-reports");
  }

  public File getTestFolder() {
    return testFolder;
  }

  public String getChromeBinary() {
    return getProperty("webdriver.chrome.bin");
  }

  public String getFirefoxBinary() {
    return getProperty("webdriver.firefox.bin");
  }

  public String getGridUrl() {
    return getProperty("grid.url");
  }

  public boolean isChromeDriverSet() {
    String chromeDriver = System.getProperty("webdriver.chrome.driver");
    if (chromeDriver == null) {
      chromeDriver = getProperty("webdriver.chrome.driver");

      if (chromeDriver != null) {
        System.setProperty("webdriver.chrome.driver", chromeDriver);
      }

      String chromeLog = getProperty("webdriver.chrome.logfile");
      if (chromeLog != null) {
        System.setProperty("webdriver.chrome.logfile", chromeLog);
      }

      String chromeVerboseLogging = getProperty("webdriver.chrome.verboseLogging");
      if (chromeVerboseLogging != null) {
        System.setProperty("webdriver.chrome.verboseLogging", chromeVerboseLogging);
      }
    }
    return StringUtils.isNotBlank(chromeDriver);
  }

  private static Properties getInstProperties(File instFolder) throws IOException {
    File propsFile = new File(instFolder, INSTITUTION_PROPS);
    Properties props = null;
    if (propsFile.exists()) {
      try (FileInputStream fis = new FileInputStream(propsFile)) {
        props = new Properties();
        props.load(fis);
      }
    }
    return props;
  }

  public String getInstitutionUrl() {
    return getInstitutionUrl(testFolder);
  }

  private String getInstitutionUrl(File instFolder) {
    boolean ssl = isSsl(instFolder);
    return getInstitutionUrl(instFolder.getName(), ssl);
  }

  public String getInstitutionUrl(String instName) {
    File instFolder = findInstitutionFolder(instName);
    return getInstitutionUrl(instFolder);
  }

  public String getInstitutionUrl(String instName, boolean https) {
    return getServerUrl(https) + instName + '/';
  }

  public boolean isAlertSupported() {
    return alertSupported;
  }

  /**
   * The autotest project folder that owns the institution fixtures, and under which results and
   * screenshots are written.
   *
   * <p>Resolved by walking up from this class's own file rather than from the working directory, so
   * that it also works when tests are launched from an IDE. The folder is recognised by what it
   * contains — an {@value #INSTITUTIONS_DIR} directory next to a {@value #BUILD_DEFINITION} —
   * rather than by its name, so the project can be renamed or moved without breaking this. Both
   * markers are needed: a compiled package path can itself contain a directory called {@value
   * #INSTITUTIONS_DIR}.
   *
   * <p>Set the {@value #BASE_FOLDER_PROPERTY} config property to override, relative to the working
   * directory.
   */
  public static File getBaseFolder() {
    if (baseFolder == null) {
      baseFolder = configuredBaseFolder().orElseGet(() -> findBaseFolderAbove(ownClassFile()));
    }
    return baseFolder;
  }

  private static Optional<File> configuredBaseFolder() {
    return config.hasPath(BASE_FOLDER_PROPERTY)
        ? Optional.of(new File(config.getString(BASE_FOLDER_PROPERTY)))
        : Optional.empty();
  }

  /** Walks up the directories containing {@code start}, stopping at the first base folder. */
  private static File findBaseFolderAbove(File start) {
    return Stream.iterate(start, Objects::nonNull, File::getParentFile)
        .filter(TestConfig::isBaseFolder)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(noBaseFolderMessage(start)));
  }

  private static String noBaseFolderMessage(File start) {
    return MessageFormat.format(
        "Could not find the autotest base folder above {0} - looked for a directory containing both"
            + " ''{1}'' and ''{2}''. Set the ''{3}'' config property to override.",
        start, INSTITUTIONS_DIR, BUILD_DEFINITION, BASE_FOLDER_PROPERTY);
  }

  private static boolean isBaseFolder(File dir) {
    return new File(dir, INSTITUTIONS_DIR).isDirectory()
        && new File(dir, BUILD_DEFINITION).isFile();
  }

  /** The file this class was loaded from, which is the starting point for the walk upwards. */
  private static File ownClassFile() {
    String name = TestConfig.class.getSimpleName() + ".class";
    String failure = "Could not resolve the location of " + name;
    URL location =
        Optional.ofNullable(TestConfig.class.getResource(name))
            .orElseThrow(() -> new IllegalStateException(failure));
    try {
      return Paths.get(location.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(failure, e);
    }
  }

  public TimeZone getBrowserTimeZone() {
    if (_browserTimeZone == null) {
      String btz = getProperty("tests.browsertimezone");
      if (btz == null) {
        _browserTimeZone = TimeZone.getDefault();
      } else {
        _browserTimeZone = TimeZone.getTimeZone(btz);
      }
    }
    return _browserTimeZone;
  }
}
