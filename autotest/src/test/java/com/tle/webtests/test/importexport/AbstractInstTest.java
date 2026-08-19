package com.tle.webtests.test.importexport;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.test.AbstractTest;
import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractInstTest extends AbstractTest {
  @Override
  protected boolean isInstitutional() {
    return false;
  }

  /**
   * Every fixture folder holding an exploded institution, as single-argument rows for a TestNG data
   * provider. Folders without one - {@code importexport}, which holds whole archives instead - are
   * not fixtures these tests can drive, and are left out.
   */
  protected static Object[][] institutionFixtures() {
    return Arrays.stream(Objects.requireNonNull(TestConfig.getInstitutionsFolder().listFiles()))
        .filter(instDir -> TestConfig.getInstitutionTree(instDir.getName()).isDirectory())
        .map(instDir -> new Object[] {instDir})
        .toArray(Object[][]::new);
  }
}
