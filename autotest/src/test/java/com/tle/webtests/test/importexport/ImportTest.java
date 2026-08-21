package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.pageobject.institution.ImportTab;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Checks that institutions exported by past releases still import, from the archives committed
 * under {@value #ARCHIVE_FIXTURES}.
 */
public class ImportTest extends AbstractInstTest {
  private static final String ARCHIVE_FIXTURES = "importexport/institutions";
  private static final List<String> ARCHIVE_EXTENSIONS = List.of(".gz", ".tgz", ".bz2", ".zip");

  /** Long enough for the oldest archives, which run every migration since they were written. */
  private static final long IMPORT_TIMEOUT_SECONDS = 360;

  @Test(dataProvider = "toImport")
  public void importInstitutions(File instFolder, String fileName) {
    String shortName = instFolder.getName();
    String institutionUrl = testConfig.getInstitutionUrl(shortName);

    InstitutionListTab listTab =
        deleteIfPresent(new InstitutionListTab(context).load(), institutionUrl);
    ImportTab importTab = listTab.importTab();

    assertTrue(
        importTab
            .importInstitution(
                institutionUrl, shortName, new File(instFolder, fileName), IMPORT_TIMEOUT_SECONDS)
            .waitForFinish());
  }

  @Test(
      dependsOnMethods = {"importInstitutions"},
      dataProvider = "toImport",
      alwaysRun = true)
  public void deleteInstitutions(File instFolder, String fileName) {
    String institutionUrl = testConfig.getInstitutionUrl(instFolder.getName());

    InstitutionListTab listTab =
        deleteIfPresent(new InstitutionListTab(context).load(), institutionUrl);
    // Leaves the console on the import tab, ready for the next archive.
    listTab.importTab();
  }

  /** Each release folder paired with the archive inside it. */
  @DataProvider(parallel = false)
  public Object[][] toImport() {
    return Arrays.stream(releaseFolders())
        .map(folder -> archiveIn(folder).map(archive -> new Object[] {folder, archive}))
        .flatMap(Optional::stream)
        .toArray(Object[][]::new);
  }

  /** The folders under {@value #ARCHIVE_FIXTURES}, one per past release. */
  private static File[] releaseFolders() {
    File archives = new File(TestConfig.getInstitutionsFolder(), ARCHIVE_FIXTURES);
    File[] folders = archives.listFiles();
    if (folders == null) {
      throw new IllegalStateException("No release archives at " + archives);
    }
    return folders;
  }

  /** The archive within one release folder, if it holds one. */
  private static Optional<String> archiveIn(File releaseFolder) {
    return Arrays.stream(Objects.requireNonNull(releaseFolder.listFiles()))
        .map(File::getName)
        .filter(ImportTest::isArchive)
        .findFirst();
  }

  private static boolean isArchive(String fileName) {
    return ARCHIVE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
  }
}
