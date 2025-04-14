package com.tle.webtests.remotetest.integration.moodle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.fest.util.Strings;
import org.testng.annotations.Factory;

@TestInstitution("moodle")
public class SyncMoodle {
  private static List<String> MOODLES = ImmutableList.of("29" /*,"25", "26", "27", "28"*/);
  private Set<String> allowedMoodles = ImmutableSet.copyOf(MOODLES);

  @Factory
  public Object[] createTests() throws Exception {
    TestConfig testConfig = new TestConfig(getClass());
    List<Object> testObjects = new ArrayList<Object>();

    String allowedMoodlesStr = testConfig.getProperty("moodle.allowed");
    if (allowedMoodlesStr != null) {
      allowedMoodles = ImmutableSet.copyOf(Arrays.asList(allowedMoodlesStr.split(",")));
    }

    for (String version : MOODLES) {
      if (allowedMoodles.contains(version)) {
        SyncMoodleTest moodleTest = new SyncMoodleTest();
        String moodleContextUrl = testConfig.getMoodleContextUrl(version);
        if (!Strings.isEmpty(moodleContextUrl)) {
          moodleTest.setMoodleUrl(moodleContextUrl);
          testObjects.add(moodleTest);
        } else {
          System.out.println(
              MessageFormat.format(
                  "ERROR: Moodle{0} has no moodle.{0}.url configured but is included in"
                      + " moodles.allowed!",
                  version));
        }
      }
    }
    return testObjects.toArray(new Object[testObjects.size()]);
  }
}
