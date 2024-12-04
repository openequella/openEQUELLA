package com.tle.webtests.remotetest.integration.moodle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.remotetest.integration.moodle.annotation.Moodles;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.fest.util.Strings;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@TestInstitution("moodle")
public class MoodleTestFactory {
  /*
   * 6.4 only supports 2.6 upwards (2.7, 2.8)
   */
  private static List<String> MOODLES = ImmutableList.of("29" /*,"25", "26", "27", "28"*/);
  private Set<String> allowedMoodles = ImmutableSet.copyOf(MOODLES);
  private TestConfig testConfig;

  @Factory
  @Parameters({"class-type"})
  public Object[] createTests(
      @Optional("com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest")
          String classType)
      throws Exception {
    testConfig = new TestConfig(getClass());
    String allowedMoodlesStr = testConfig.getProperty("moodle.allowed");
    if (allowedMoodlesStr != null) {
      allowedMoodles = ImmutableSet.copyOf(Arrays.asList(allowedMoodlesStr.split(",")));
    }
    List<AbstractMoodleTest> testObjects = new ArrayList<AbstractMoodleTest>();
    URL dirUrl =
        getClass().getClassLoader().getResource("com/tle/webtests/remotetest/integration/moodle");
    File moodleTests = new File(dirUrl.toURI());
    recurse(moodleTests, "com.tle.webtests.remotetest.integration.moodle.", testObjects, classType);
    Collections.sort(
        testObjects,
        new Comparator<AbstractMoodleTest>() {
          @Override
          public int compare(AbstractMoodleTest o1, AbstractMoodleTest o2) {
            return Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());
          }
        });
    return testObjects.toArray(new Object[testObjects.size()]);
  }

  private void recurse(
      File dir, String packagePrefix, List<AbstractMoodleTest> testObjects, String classType)
      throws Exception {
    File[] files = dir.listFiles();
    for (File file : files) {
      String filename = file.getName();
      if (file.isDirectory()) {
        recurse(file, packagePrefix + filename + '.', testObjects, classType);
      } else if (file.isFile() && filename.endsWith(".class") && filename.indexOf('$') == -1) {
        Class<?> clazz =
            getClass()
                .getClassLoader()
                .loadClass(packagePrefix + filename.substring(0, filename.length() - 6));

        if (Class.forName(classType).isAssignableFrom(clazz)
            && !Modifier.isAbstract(clazz.getModifiers())) {
          List<String> runMoodles = MOODLES;
          int order = 1000;
          Moodles moodles = clazz.getAnnotation(Moodles.class);
          if (moodles != null) {
            if (moodles.value().length > 0) {
              runMoodles = ImmutableList.copyOf(moodles.value());
            }

            order = moodles.order();
          }

          for (String version : runMoodles) {
            if (allowedMoodles.contains(version)) {
              AbstractMoodleTest moodleTest = AbstractMoodleTest.class.cast(clazz.newInstance());
              String moodleContextUrl = testConfig.getMoodleContextUrl(version);
              if (!Strings.isEmpty(moodleContextUrl)) {
                moodleTest.setMoodleUrl(moodleContextUrl);
                moodleTest.setOrder(order);
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
        }
      }
    }
  }
}
